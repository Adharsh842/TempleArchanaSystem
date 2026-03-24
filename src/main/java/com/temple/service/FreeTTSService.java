package com.temple.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FreeTTSService {

    private static final String GOOGLE_TTS =
        "https://translate.google.com/translate_tts" +
        "?ie=UTF-8&client=tw-ob&ttsspeed=1&tl=%s&q=%s";

    // ════════════════════════════════════════════════
    // GET CORRECT SAVE PATH AT RUNTIME
    // ════════════════════════════════════════════════
    private String getSavePath() {
        // Spring Boot serves static files from target/classes/static/
        // at runtime — this is where we must save
        String[] paths = {
            "target/classes/static/audio/",
            "src/main/resources/static/audio/"
        };

        for (String path : paths) {
            File dir = new File(path);
            if (dir.exists()) {
                System.out.println("Audio save path: " + dir.getAbsolutePath());
                return path;
            }
            if (dir.mkdirs()) {
                System.out.println("Created audio dir: " + dir.getAbsolutePath());
                return path;
            }
        }

        // Last resort — use target path
        new File("target/classes/static/audio/").mkdirs();
        return "target/classes/static/audio/";
    }

    // ════════════════════════════════════════════════
    // PUBLIC METHODS
    // ════════════════════════════════════════════════

    public String generateTamilAudio(String text, String bookingId)
            throws Exception {
        return generateAudio(text, "ta", "tamil_" + bookingId);
    }

    public String generateEnglishAudio(String text, String bookingId)
            throws Exception {
        return generateAudio(text, "en", "english_" + bookingId);
    }

    public String generateAudio(String text, String lang, String fileName)
            throws Exception {

        String savePath = getSavePath();
        String mp3File  = fileName + ".mp3";
        String fullPath = savePath + mp3File;

        System.out.println("Generating audio: " + fullPath);

        // Split into chunks
        String[] chunks = splitChunks(text, 180);
        System.out.println("Total chunks: " + chunks.length);

        if (chunks.length == 1) {
            downloadOne(chunks[0], lang, fullPath);
        } else {
            downloadAndMerge(chunks, lang, fullPath, savePath);
        }

        // Verify file was saved
        File saved = new File(fullPath);
        System.out.println("File exists: " + saved.exists() +
                           " | Size: " + saved.length() + " bytes");

        return "/audio/" + mp3File;
    }

    // ════════════════════════════════════════════════
    // DOWNLOAD ONE CHUNK FROM GOOGLE TTS
    // ════════════════════════════════════════════════
    private void downloadOne(String text, String lang, String savePath)
            throws Exception {

        String encoded = URLEncoder.encode(text, "UTF-8");
        String urlStr  = String.format(GOOGLE_TTS, lang, encoded);

        System.out.println("Calling: " + urlStr.substring(0, 80) + "...");

        URL               url  = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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
        System.out.println("Google TTS status: " + status);

        if (status != 200) {
            InputStream err = conn.getErrorStream();
            String errMsg = "";
            if (err != null) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(err));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                errMsg = sb.toString();
            }
            throw new Exception("Google TTS HTTP " + status +
                                ": " + errMsg);
        }

        // Save MP3
        try (InputStream is  = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(savePath)) {
            byte[] buf   = new byte[4096];
            int    n;
            int    total = 0;
            while ((n = is.read(buf)) != -1) {
                fos.write(buf, 0, n);
                total += n;
            }
            System.out.println("Saved " + total + " bytes → " + savePath);
        }
    }

    // ════════════════════════════════════════════════
    // DOWNLOAD MULTIPLE CHUNKS AND MERGE
    // ════════════════════════════════════════════════
    private void downloadAndMerge(String[] chunks, String lang,
            String finalPath, String tempDir) throws Exception {

        List<String> temps = new ArrayList<>();

        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] == null || chunks[i].trim().isEmpty()) continue;

            String tmp = tempDir + "tmp_" + i + "_" +
                         System.currentTimeMillis() + ".mp3";
            System.out.println("Downloading chunk " + (i+1) +
                               "/" + chunks.length + ": " + chunks[i]);
            downloadOne(chunks[i], lang, tmp);
            temps.add(tmp);

            // Delay between requests — avoid rate limiting
            if (i < chunks.length - 1) Thread.sleep(500);
        }

        // Merge all temp MP3 files into one
        System.out.println("Merging " + temps.size() + " files...");
        try (FileOutputStream fos = new FileOutputStream(finalPath)) {
            for (String tmp : temps) {
                File f = new File(tmp);
                if (f.exists()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        byte[] buf = new byte[4096];
                        int    n;
                        while ((n = fis.read(buf)) != -1) {
                            fos.write(buf, 0, n);
                        }
                    }
                    f.delete();
                    System.out.println("Merged: " + tmp);
                }
            }
        }
        System.out.println("Final merged file: " + finalPath);
    }

    // ════════════════════════════════════════════════
    // SPLIT TEXT INTO CHUNKS (max 180 chars each)
    // ════════════════════════════════════════════════
    private String[] splitChunks(String text, int maxLen) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{ "Om. Govinda." };
        }
        if (text.length() <= maxLen) {
            return new String[]{ text };
        }

        List<String>  result = new ArrayList<>();
        String[]      sents  = text.split("(?<=\\.) ");
        StringBuilder cur    = new StringBuilder();

        for (String s : sents) {
            s = s.trim();
            String add = s + " ";
            if (cur.length() + add.length() > maxLen) {
                if (cur.length() > 0) {
                    result.add(cur.toString().trim());
                    cur = new StringBuilder();
                }
                if (add.length() > maxLen) {
                    String[] words = add.split(" ");
                    for (String w : words) {
                        if (cur.length() + w.length() + 1 > maxLen) {
                            if (cur.length() > 0) {
                                result.add(cur.toString().trim());
                                cur = new StringBuilder();
                            }
                        }
                        cur.append(w).append(" ");
                    }
                } else {
                    cur.append(add);
                }
            } else {
                cur.append(add);
            }
        }

        if (cur.length() > 0) result.add(cur.toString().trim());

        // Filter empty
        List<String> clean = new ArrayList<>();
        for (String r : result) {
            if (r != null && !r.trim().isEmpty()) clean.add(r);
        }

        return clean.isEmpty()
            ? new String[]{ text }
            : clean.toArray(new String[0]);
    }

    // ════════════════════════════════════════════════
    // TAMIL TEXT BUILDER — Real Tamil Unicode
    // ════════════════════════════════════════════════
    public String buildTamilText(String name, String gothram,
                                  String nakshatram, String archanaType) {
        return
            "ஓம். ஓம். ஓம். " +
            "சுபம் கரோதி கல்யாணம். " +
            "ஆரோக்யம் தன சம்பதா. " +
            "சத்ரு புத்தி விநாசாய. " +
            "தீப ஜோதிர் நமோஸ்துதே. " +
            "அடியார்களே. " +
            name       + " அவர்களுக்கு. " +
            gothram    + " கோத்திரம். " +
            nakshatram + " நட்சத்திரம். " +
            tamilChant(archanaType) +
            "சர்வ மங்கள பிராப்திரஸ்து. " +
            "சர்வ விக்ன நிவாரணம். " +
            "ஆயு ஆரோக்யம் ஐஸ்வர்யம். " +
            "தீர்க்க ஆயுசு பிராப்திரஸ்து. " +
            "புத்ர பௌத்ர அபிவிருத்தி ராஸ்து. " +
            "ஓம் சாந்தி. சாந்தி. சாந்திஹி. " +
            "ஜெய் ஸ்ரீ வெங்கடேஸ்வரா. " +
            "கோவிந்தா. கோவிந்தா. கோவிந்தா.";
    }

    private String tamilChant(String t) {
        if (t == null) return "அர்ச்சனை சமர்ப்பணம். ஓம் நமோ நாராயணாய. ";
        switch (t.trim()) {
            case "Pushparchana":
                return "புஷ்ப அர்ச்சனை சமர்ப்பணம். " +
                       "ஓம் நமோ நாராயணாய. " +
                       "சத துளசி புஷ்பம் சமர்ப்யாமி. ";
            case "Kumkumarchana":
                return "குங்குமார்ச்சனை சமர்ப்பணம். " +
                       "ஓம் சக்தி நமஹ. " +
                       "குங்குமம் சமர்ப்யாமி. ";
            case "Abhishekam":
                return "திருவாபிஷேகம் சமர்ப்பணம். " +
                       "ஓம் நமச்சிவாய. " +
                       "பஞ்சாமிர்த அபிஷேகம் சமர்ப்யாமி. ";
            case "Sahasranamam":
                return "சஹஸ்ரநாம அர்ச்சனை சமர்ப்பணம். " +
                       "ஓம் நமோ பகவதே வாசுதேவாய. " +
                       "சஹஸ்ர புஷ்பம் சமர்ப்யாமி. ";
            case "Ganapathi Homam":
                return "கணபதி ஹோமம் சமர்ப்பணம். " +
                       "ஓம் கம் கணபதயே நமஹ. " +
                       "ஸ்வாஹா. பூர்ணாஹுதி சமர்ப்யாமி. ";
            default:
                return "அர்ச்சனை சமர்ப்பணம். ஓம் நமோ நாராயணாய. ";
        }
    }

    // ════════════════════════════════════════════════
    // ENGLISH TEXT BUILDER
    // ════════════════════════════════════════════════
    public String buildEnglishText(String name, String gothram,
                                    String nakshatram, String archanaType) {
        return
            "Om. Om. Om. " +
            "Shubham Karoti Kalyanam. " +
            "Arogyam Dhana Sampada. " +
            "Shatru Buddhi Vinashaya. " +
            name       + " avargalukku. " +
            gothram    + " gothram. " +
            nakshatram + " nakshatram. " +
            engChant(archanaType) +
            "Sarva Mangala Prapthirastu. " +
            "Ayu Arogyam Aishwaryam. " +
            "Dheerga Ayushu Prapthirastu. " +
            "Om Shanti. Shanti. Shantihi. " +
            "Jai Sri Venkateswara. " +
            "Govinda. Govinda. Govinda.";
    }

    private String engChant(String t) {
        if (t == null) return "Archana samarpanam. Om Namo Narayanaya. ";
        switch (t.trim()) {
            case "Pushparchana":
                return "Pushpa Archana samarpanam. Om Namo Narayanaya. Pushpam samarpayami. ";
            case "Kumkumarchana":
                return "Kumkuma Archana samarpanam. Om Shakti Namaha. Kumkumam samarpayami. ";
            case "Abhishekam":
                return "Abhishekam samarpanam. Om Namah Shivaya. Panchamrutha Abhishekam samarpayami. ";
            case "Sahasranamam":
                return "Sahasranama samarpanam. Om Namo Vasudevaya. Sahasra pushpam samarpayami. ";
            case "Ganapathi Homam":
                return "Ganapathi Homam samarpanam. Om Gam Ganapataye Namaha. Poornahuti samarpayami. ";
            default:
                return "Archana samarpanam. Om Namo Narayanaya. ";
        }
    }

    // ════════════════════════════════════════════════
    // LEGACY COMPATIBILITY METHODS
    // ════════════════════════════════════════════════
    public String buildTamilTransliterationText(String name, String gothram,
            String nakshatram, String archanaType) {
        return "Om. Om. Om. Shubham Karodi Kalyanam. " +
               name + " avargalukku. " + gothram + " gothiram. " +
               nakshatram + " natchathiram. " +
               "Sarva Mangalam. Govindaa. Govindaa. Govindaa.";
    }

    public String buildTamilAudioUrl(String text) {
        try {
            return "https://translate.google.com/translate_tts" +
                   "?ie=UTF-8&client=tw-ob&ttsspeed=1&tl=ta&q=" +
                   URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) { return ""; }
    }
}