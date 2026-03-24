package com.temple.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FishAudioService {

    @Value("${fish.audio.api.key:}")
    private String apiKey;

    @Value("${fish.audio.model.id:}")
    private String modelId;

    private static final String API_URL =
        "https://api.fish.audio/v1/tts";

    // ════════════════════════════════════════════════
    // MAIN METHOD — Generate Priest Voice
    // Tries Fish Audio first → falls back to Google TTS
    // ════════════════════════════════════════════════
    public String generatePriestVoice(
            String text,
            String bookingId,
            String lang) throws Exception {

        System.out.println("Generating priest voice for: " +
            bookingId + " | lang: " + lang);

        // Try Fish Audio API first if key is configured
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                System.out.println(
                    "Trying Fish Audio API...");
                return callFishAudioAPI(text, bookingId, lang);

            } catch (Exception e) {
                System.err.println(
                    "Fish Audio failed: " + e.getMessage());
                System.out.println(
                    "Falling back to Google TTS...");
                // Fall through to Google TTS fallback
            }
        } else {
            System.out.println(
                "No Fish Audio key — using Google TTS");
        }

        // Fallback to Google TTS (free, always works)
        return googleTTSFallback(text, bookingId, lang);
    }

    // ════════════════════════════════════════════════
    // FISH AUDIO API CALL
    // ════════════════════════════════════════════════
    private String callFishAudioAPI(
            String text,
            String bookingId,
            String lang) throws Exception {

        String savePath = getSavePath();
        String fileName = "fish_" + lang + "_" +
                          bookingId + ".mp3";
        String fullPath = savePath + fileName;

        System.out.println("Fish Audio API call...");
        System.out.println("Text length: " +
            text.length() + " chars");

        String jsonBody = buildRequestBody(text);

        URL               url  = new URL(API_URL);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty(
            "Authorization", "Bearer " + apiKey);
        conn.setRequestProperty(
            "Content-Type", "application/json");
        conn.setRequestProperty(
            "Accept", "audio/mpeg");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();
        }

        int status = conn.getResponseCode();
        System.out.println("Fish Audio HTTP: " + status);

        if (status == 402) {
            throw new Exception(
                "Fish Audio Insufficient Balance. " +
                "Top up at fish.audio or get new account.");
        }

        if (status == 401) {
            throw new Exception(
                "Fish Audio Invalid API Key. " +
                "Check fish.audio.api.key in properties.");
        }

        if (status == 429) {
            throw new Exception(
                "Fish Audio Rate Limit exceeded. " +
                "Try again later.");
        }

        if (status != 200) {
            StringBuilder err = new StringBuilder();
            InputStream   es  = conn.getErrorStream();
            if (es != null) {
                try (BufferedReader br =
                        new BufferedReader(
                            new InputStreamReader(
                                es, "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        err.append(line);
                    }
                }
            }
            throw new Exception(
                "Fish Audio error " + status +
                ": " + err.toString());
        }

        // Save MP3
        try (InputStream      is  = conn.getInputStream();
             FileOutputStream fos =
                new FileOutputStream(fullPath)) {

            byte[] buf   = new byte[4096];
            int    n;
            int    total = 0;
            while ((n = is.read(buf)) != -1) {
                fos.write(buf, 0, n);
                total += n;
            }
            System.out.println("Fish Audio saved: " +
                total + " bytes → " + fullPath);
        }

        // Verify file
        File saved = new File(fullPath);
        if (!saved.exists() || saved.length() == 0) {
            throw new Exception(
                "Fish Audio: saved file is empty!");
        }

        System.out.println("✅ Fish Audio complete: " +
            saved.length() + " bytes");
        return "/audio/" + fileName;
    }

    // ════════════════════════════════════════════════
    // GOOGLE TTS FALLBACK
    // Free, no API key, works always
    // Used when Fish Audio balance is low or unavailable
    // ════════════════════════════════════════════════
    private String googleTTSFallback(
            String text,
            String bookingId,
            String lang) throws Exception {

        String savePath = getSavePath();
        String langCode = "tamil".equals(lang) ? "ta" : "en";
        String fileName = "fish_" + lang + "_" +
                          bookingId + ".mp3";
        String fullPath = savePath + fileName;

        System.out.println("Google TTS fallback: lang=" +
            langCode + " | text length=" + text.length());

        // Split into chunks (Google limit ~200 chars)
        String[] chunks = splitForGoogle(text, 180);
        System.out.println("Google TTS chunks: " +
            chunks.length);

        if (chunks.length == 1) {
            downloadGoogleChunk(
                chunks[0], langCode, fullPath);
        } else {
            downloadAndMergeGoogle(
                chunks, langCode, fullPath, savePath);
        }

        // Verify
        File f = new File(fullPath);
        System.out.println("Google TTS done: " +
            f.length() + " bytes → " + fullPath);

        return "/audio/" + fileName;
    }

    // ── Download one Google TTS chunk ───────────────
    private void downloadGoogleChunk(
            String text,
            String langCode,
            String savePath) throws Exception {

        String encoded = URLEncoder.encode(text, "UTF-8");
        String urlStr  =
            "https://translate.google.com/translate_tts" +
            "?ie=UTF-8&client=tw-ob&ttsspeed=1&tl=" +
            langCode + "&q=" + encoded;

        URL               url  = new URL(urlStr);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36");
        conn.setRequestProperty("Referer",
            "https://translate.google.com/");
        conn.setRequestProperty("Accept", "audio/mpeg, */*");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        System.out.println("Google TTS chunk status: " + status);

        if (status != 200) {
            throw new Exception(
                "Google TTS chunk failed: HTTP " + status);
        }

        try (InputStream      is  = conn.getInputStream();
             FileOutputStream fos =
                new FileOutputStream(savePath)) {

            byte[] buf   = new byte[4096];
            int    n;
            int    total = 0;
            while ((n = is.read(buf)) != -1) {
                fos.write(buf, 0, n);
                total += n;
            }
            System.out.println("Google chunk saved: " +
                total + " bytes");
        }
    }

    // ── Download multiple chunks and merge ──────────
    private void downloadAndMergeGoogle(
            String[] chunks,
            String   langCode,
            String   finalPath,
            String   tempDir) throws Exception {

        List<String> temps = new ArrayList<>();

        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] == null ||
                chunks[i].trim().isEmpty()) continue;

            String tmp = tempDir + "gtmp_" + i + "_" +
                System.currentTimeMillis() + ".mp3";

            System.out.println("Downloading chunk " +
                (i+1) + "/" + chunks.length);
            downloadGoogleChunk(chunks[i], langCode, tmp);
            temps.add(tmp);

            // Small delay between requests
            if (i < chunks.length - 1) Thread.sleep(400);
        }

        // Merge all temp files
        System.out.println("Merging " +
            temps.size() + " chunks...");

        try (FileOutputStream fos =
                new FileOutputStream(finalPath)) {

            for (String tmp : temps) {
                File f = new File(tmp);
                if (f.exists()) {
                    try (FileInputStream fis =
                            new FileInputStream(f)) {
                        byte[] buf = new byte[4096];
                        int    n;
                        while ((n = fis.read(buf)) != -1) {
                            fos.write(buf, 0, n);
                        }
                    }
                    f.delete();
                }
            }
        }
        System.out.println("Merge complete: " + finalPath);
    }

    // ── Split text into Google-safe chunks ──────────
    private String[] splitForGoogle(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return new String[]{ text };
        }

        List<String>  result = new ArrayList<>();
        String[]      sents  = text.split("(?<=\\.) ");
        StringBuilder cur    = new StringBuilder();

        for (String s : sents) {
            s = s.trim();
            if (s.isEmpty()) continue;

            if (cur.length() + s.length() + 2 > maxLen) {
                if (cur.length() > 0) {
                    result.add(cur.toString().trim());
                    cur = new StringBuilder();
                }
                // Long sentence → split by words
                if (s.length() > maxLen) {
                    String[] words = s.split(" ");
                    for (String w : words) {
                        if (cur.length() + w.length() + 1
                                > maxLen) {
                            if (cur.length() > 0) {
                                result.add(
                                    cur.toString().trim());
                                cur = new StringBuilder();
                            }
                        }
                        cur.append(w).append(" ");
                    }
                } else {
                    cur.append(s).append(". ");
                }
            } else {
                cur.append(s).append(". ");
            }
        }

        if (cur.length() > 0) {
            result.add(cur.toString().trim());
        }

        // Filter empty
        List<String> clean = new ArrayList<>();
        for (String r : result) {
            if (r != null && !r.trim().isEmpty()) {
                clean.add(r);
            }
        }

        return clean.isEmpty()
            ? new String[]{ text }
            : clean.toArray(new String[0]);
    }

    // ════════════════════════════════════════════════
    // BUILD FISH AUDIO REQUEST BODY
    // ════════════════════════════════════════════════
    private String buildRequestBody(String text) {
        String escaped = escapeJson(text);

        // With voice model ID
        if (modelId != null && !modelId.trim().isEmpty()) {
            return "{" +
                "\"text\": \""         + escaped  + "\"," +
                "\"reference_id\": \"" + modelId  + "\"," +
                "\"format\": \"mp3\"," +
                "\"mp3_bitrate\": 128," +
                "\"normalize\": true," +
                "\"latency\": \"normal\"" +
                "}";
        }

        // Without model ID — Fish Audio default voice
        return "{" +
            "\"text\": \""  + escaped + "\"," +
            "\"format\": \"mp3\"," +
            "\"mp3_bitrate\": 128," +
            "\"normalize\": true," +
            "\"latency\": \"normal\"" +
            "}";
    }

    // ════════════════════════════════════════════════
    // BUILD PRIEST TEXT — Pooja Style
    // Authentic Tamil/English archana script
    // ════════════════════════════════════════════════
    public String buildPriestText(
            String name,
            String gothram,
            String nakshatram,
            String archanaType,
            String lang) {

        if ("tamil".equalsIgnoreCase(lang)) {
            return buildTamilPriestText(
                name, gothram, nakshatram, archanaType);
        }
        return buildEnglishPriestText(
            name, gothram, nakshatram, archanaType);
    }

    // ── Tamil Priest Archana Script ──────────────────
    private String buildTamilPriestText(
            String name,
            String gothram,
            String nakshatram,
            String archanaType) {

        return
            // Opening invocation
            "ஓம் நமோ நாராயணாய. " +
            "ஓம் நமோ நாராயணாய. " +
            "ஓம் நமோ நாராயணாய. " +

            // Auspicious verse
            "சுபம் கரோதி கல்யாணம். " +
            "ஆரோக்யம் தன சம்பதா. " +
            "சத்ரு புத்தி விநாசாய. " +
            "தீப ஜோதிர் நமோஸ்துதே. " +

            // Temple announcement
            "இன்று இந்த மங்கல நேரத்தில். " +
            "ஸ்ரீ வெங்கடேஸ்வர பெருமாளுக்கு. " +

            // Devotee details
            name       + " அவர்களுக்காக. " +
            gothram    + " கோத்திரத்தினர். " +
            nakshatram + " நட்சத்திரத்தினர். " +

            // Archana specific chant
            getPriestTamilChant(archanaType) +

            // Blessings
            "சர்வ மங்கள பிராப்திரஸ்து. " +
            "சர்வ விக்ன நிவாரணம். " +
            "ஆயுர் ஆரோக்யம் ஐஸ்வர்யம். " +
            "தீர்க்க ஆயுசு பிராப்திரஸ்து. " +
            "புத்ர பௌத்ர அபிவிருத்தி ராஸ்து. " +
            "இஷ்ட கார்ய சித்திரஸ்து. " +

            // Closing
            "ஓம் சாந்தி. சாந்தி. சாந்திஹி. " +
            "ஹரே கிருஷ்ண. ஹரே கிருஷ்ண. " +
            "கோவிந்தா. கோவிந்தா. கோவிந்தா. " +
            "ஜெய் ஸ்ரீ வெங்கடேஸ்வரா.";
    }

    // ── English Priest Archana Script ────────────────
    private String buildEnglishPriestText(
            String name,
            String gothram,
            String nakshatram,
            String archanaType) {

        return
            "Om Namo Narayanaya. " +
            "Om Namo Narayanaya. " +
            "Om Namo Narayanaya. " +

            "Shubham Karoti Kalyanam. " +
            "Arogyam Dhana Sampada. " +
            "Shatru Buddhi Vinashaya. " +
            "Deepa Jyotir Namostute. " +

            "On this auspicious occasion. " +
            "We offer prayers to " +
            "Lord Sri Venkateswara. " +

            "On behalf of devotee " + name + ". " +
            "Of " + gothram + " gothram. " +
            "Born under " + nakshatram + " star. " +

            getPriestEngChant(archanaType) +

            "May the Lord bless you with. " +
            "Long life and good health. " +
            "Prosperity and happiness. " +
            "May all your wishes be fulfilled. " +
            "Sarva Mangala Prapthirastu. " +

            "Om Shanti. Shanti. Shantihi. " +
            "Hare Krishna. Hare Krishna. " +
            "Govinda. Govinda. Govinda. " +
            "Jai Sri Venkateswara.";
    }

    // ── Tamil Archana Chants by Type ─────────────────
    private String getPriestTamilChant(String t) {
        if (t == null) {
            return
                "அர்ச்சனை சமர்ப்பணம். " +
                "ஓம் நமோ நாராயணாய. ";
        }
        switch (t.trim()) {
            case "Pushparchana":
                return
                    "சதநாம புஷ்பார்ச்சனை சமர்ப்பணம். " +
                    "ஓம் நமோ நாராயணாய. " +
                    "ஓம் விஷ்ணவே நமஹ. " +
                    "திருவேங்கடமுடையான் திருவடிகளே சரணம். " +
                    "புஷ்பம் சமர்ப்யாமி. ";

            case "Kumkumarchana":
                return
                    "குங்கும அர்ச்சனை சமர்ப்பணம். " +
                    "ஓம் சக்தி நமஹ. " +
                    "ஓம் பராசக்தியே நமஹ. " +
                    "ஸ்ரீ மஹா லக்ஷ்மி திருவடிகளே சரணம். " +
                    "குங்குமம் சமர்ப்யாமி. ";

            case "Abhishekam":
                return
                    "திருவாபிஷேக சமர்ப்பணம். " +
                    "ஓம் நமச்சிவாய. " +
                    "ஓம் மஹாதேவாய நமஹ. " +
                    "பால அபிஷேகம். தயிர் அபிஷேகம். " +
                    "தேன் அபிஷேகம். நெய் அபிஷேகம். " +
                    "பஞ்சாமிர்த அபிஷேகம் சமர்ப்யாமி. ";

            case "Sahasranamam":
                return
                    "சஹஸ்ரநாம அர்ச்சனை சமர்ப்பணம். " +
                    "ஓம் நமோ பகவதே வாசுதேவாய. " +
                    "விஷ்ணு சஹஸ்ரநாமம். " +
                    "ஆயிரம் திருநாமங்களால் அர்ச்சனை. " +
                    "சஹஸ்ர புஷ்பம் சமர்ப்யாமி. ";

            case "Ganapathi Homam":
                return
                    "கணபதி ஹோம சமர்ப்பணம். " +
                    "ஓம் கம் கணபதயே நமஹ. " +
                    "ஓம் கணேசாய நமஹ. " +
                    "விக்ன நிவாரணாய. சர்வ சித்தி பிரதாய. " +
                    "ஸ்வாஹா. ஸ்வாஹா. ஸ்வாஹா. " +
                    "பூர்ணாஹுதி சமர்ப்யாமி. ";

            default:
                return
                    "அர்ச்சனை சமர்ப்பணம். " +
                    "ஓம் நமோ நாராயணாய. ";
        }
    }

    // ── English Archana Chants by Type ───────────────
    private String getPriestEngChant(String t) {
        if (t == null) {
            return "Archana samarpanam. ";
        }
        switch (t.trim()) {
            case "Pushparchana":
                return
                    "We offer Shatanama Pushparchana. " +
                    "Om Namo Narayanaya. " +
                    "Om Vishnave Namaha. " +
                    "Sacred flowers offered at lotus feet. ";

            case "Kumkumarchana":
                return
                    "We offer Kumkuma Archana. " +
                    "Om Shakti Namaha. " +
                    "Om Para Shaktyai Namaha. " +
                    "Sacred kumkum offered with devotion. ";

            case "Abhishekam":
                return
                    "We perform the sacred Abhishekam. " +
                    "Om Namah Shivaya. " +
                    "Panchamrutha Abhishekam. " +
                    "With milk, curd, honey, ghee and fruits. ";

            case "Sahasranamam":
                return
                    "We offer Sahasranama Archana. " +
                    "Om Namo Bhagavate Vasudevaya. " +
                    "One thousand divine names are chanted. " +
                    "Sahasra pushpam samarpayami. ";

            case "Ganapathi Homam":
                return
                    "We perform Ganapathi Homam. " +
                    "Om Gam Ganapataye Namaha. " +
                    "Vighna Nivaranaya. " +
                    "Sarva Siddhi Pradaya. " +
                    "Swaha. Swaha. Swaha. " +
                    "Poornahuti samarpayami. ";

            default:
                return
                    "Archana samarpanam. " +
                    "Om Namo Narayanaya. ";
        }
    }

    // ════════════════════════════════════════════════
    // GET SAVE PATH — local vs cloud
    // ════════════════════════════════════════════════
    private String getSavePath() {
        boolean isCloud =
            System.getenv("RENDER_ENVIRONMENT") != null ||
            System.getenv("RAILWAY_ENVIRONMENT") != null;

        String path = isCloud
            ? "/tmp/audio/"
            : "target/classes/static/audio/";

        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Audio dir created: " +
                dir.getAbsolutePath() +
                " → " + created);
        }
        return path;
    }

    // ════════════════════════════════════════════════
    // JSON ESCAPE
    // ════════════════════════════════════════════════
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ");
    }
}
