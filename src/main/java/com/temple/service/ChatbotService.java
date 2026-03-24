package com.temple.service;

import com.temple.entity.ChatMessage;
import com.temple.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatbotService {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private DonationService donationService;

    // ════════════════════════════════════════════════
    // PROCESS USER MESSAGE — Main entry point
    // ════════════════════════════════════════════════
    public Map<String, Object> processMessage(
            String userMessage,
            String sessionId,
            String language) {

        Map<String, Object> result = new HashMap<>();

        // Detect intent
        String intent  = detectIntent(userMessage);
        String lang    = detectLanguage(
            userMessage, language);

        // Generate response based on intent
        String response = generateResponse(
            userMessage, intent, lang, sessionId);

        // Save to database
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setUserMessage(userMessage);
        msg.setBotResponse(response);
        msg.setIntent(intent);
        msg.setLanguage(lang);
        msg.setCreatedAt(LocalDateTime.now());
        chatRepo.save(msg);

        // Build result
        result.put("response",    response);
        result.put("intent",      intent);
        result.put("language",    lang);
        result.put("sessionId",   sessionId);
        result.put("suggestions", getSuggestions(intent));
        result.put("showDonation",
            intent.equals("DONATION"));
        result.put("showBooking",
            intent.equals("BOOKING"));

        System.out.println("Chat: [" + intent + "] " +
            userMessage.substring(0,
                Math.min(30, userMessage.length())));

        return result;
    }

    // ════════════════════════════════════════════════
    // INTENT DETECTION — keyword based
    // ════════════════════════════════════════════════
    private String detectIntent(String message) {
        String m = message.toLowerCase().trim();

        // DONATION keywords
        if (containsAny(m, new String[]{
            "donat", "give money", "contribute",
            "help temple", "fund", "annadhanam",
            "நன்கொடை", "கொடுக்க", "உதவி",
            "தான"})) {
            return "DONATION";
        }

        // BOOKING keywords
        if (containsAny(m, new String[]{
            "book", "archana", "pooja", "schedule",
            "appointment", "வேண்டும்", "பண்ண",
            "புஷ்பார்ச்சனை", "அபிஷேகம்"})) {
            return "BOOKING";
        }

        // EMOTIONAL keywords
        if (containsAny(m, new String[]{
            "stress", "sad", "depress", "worried",
            "anxious", "lonely", "cry", "pain",
            "problem", "கஷ்டம்", "துக்கம்",
            "வலி", "பயம்", "கவலை", "அழுகிறேன்"})) {
            return "EMOTIONAL";
        }

        // GUIDANCE keywords
        if (containsAny(m, new String[]{
            "how to", "guide", "steps", "method",
            "procedure", "explain", "tell me",
            "எப்படி", "விளக்கு", "சொல்லு",
            "வழிகாட்டு"})) {
            return "GUIDANCE";
        }

        // INFO keywords
        if (containsAny(m, new String[]{
            "timing", "time", "open", "close",
            "hours", "price", "cost", "location",
            "நேரம்", "இடம்", "விலை", "திறக்கும்"})) {
            return "INFO";
        }

        // GREETING
        if (containsAny(m, new String[]{
            "hello", "hi", "vanakkam", "namaste",
            "வணக்கம்", "நமஸ்காரம்", "hey"})) {
            return "GREETING";
        }

        return "GENERAL";
    }

    // ════════════════════════════════════════════════
    // RESPONSE GENERATOR
    // ════════════════════════════════════════════════
    private String generateResponse(
            String message,
            String intent,
            String lang,
            String sessionId) {

        switch (intent) {
            case "GREETING":
                return getGreetingResponse(lang);
            case "GUIDANCE":
                return getGuidanceResponse(message, lang);
            case "EMOTIONAL":
                return getEmotionalResponse(lang);
            case "DONATION":
                return getDonationResponse(lang);
            case "BOOKING":
                return getBookingResponse(lang);
            case "INFO":
                return getInfoResponse(message, lang);
            default:
                return getGeneralResponse(lang);
        }
    }

    // ── GREETING ─────────────────────────────────────
    private String getGreetingResponse(String lang) {
        if ("tamil".equals(lang)) {
            return "🙏 வணக்கம்! ஸ்ரீ வெங்கடேஸ்வர கோவிலுக்கு நல்வரவு!\n\n" +
                   "நான் உங்கள் ஆன்மீக வழிகாட்டி. " +
                   "பூஜை, தான்தர்மம், அல்லது " +
                   "எந்த ஆன்மீக விஷயத்திலும் " +
                   "உங்களுக்கு உதவ தயாராக இருக்கிறேன். " +
                   "என்ன உதவி வேண்டும்? 🛕";
        }
        return "🙏 Vanakkam! Welcome to " +
               "Sri Venkateswara Temple!\n\n" +
               "I am your spiritual guide. " +
               "I can help you with pooja guidance, " +
               "donations, bookings, and " +
               "spiritual comfort. " +
               "How may I assist you today? 🛕";
    }

    // ── GUIDANCE ─────────────────────────────────────
    private String getGuidanceResponse(
            String message, String lang) {

        String m = message.toLowerCase();

        // Pushparchana guidance
        if (containsAny(m, new String[]{
            "pushp", "flower", "புஷ்ப"})) {
            return getPushparchanaGuide(lang);
        }

        // Abhishekam guidance
        if (containsAny(m, new String[]{
            "abhishek", "abishekam", "அபிஷேக"})) {
            return getAbhishekamGuide(lang);
        }

        // Sahasranamam guidance
        if (containsAny(m, new String[]{
            "sahasra", "1000", "சஹஸ்ர"})) {
            return getSahasranamamGuide(lang);
        }

        // General pooja guide
        return getGeneralPoojaGuide(lang);
    }

    private String getPushparchanaGuide(String lang) {
        if ("tamil".equals(lang)) {
            return "🌸 **புஷ்பார்ச்சனை வழிமுறை:**\n\n" +
                   "**தேவையானவை:**\n" +
                   "• 108 துளசி இலைகள் அல்லது மலர்கள்\n" +
                   "• குங்குமம், விபூதி\n" +
                   "• தேங்காய், வாழைப்பழம்\n\n" +
                   "**படிகள்:**\n" +
                   "1️⃣ குளித்து சுத்தமான ஆடை அணியுங்கள்\n" +
                   "2️⃣ மனதை அமைதிப்படுத்திக்கொள்ளுங்கள்\n" +
                   "3️⃣ ஸ்ரீ வெங்கடேஸ்வரருக்கு\n" +
                   "   ஒவ்வொரு மலராக 108 நாமங்கள்\n" +
                   "   சொல்லி சமர்ப்பியுங்கள்\n" +
                   "4️⃣ 'ஓம் நமோ நாராயணாய' என்று\n" +
                   "   ஒவ்வொரு முறையும் சொல்லுங்கள்\n" +
                   "5️⃣ இறுதியில் தீபம் ஏற்றி ஆரத்தி காட்டுங்கள்\n\n" +
                   "💰 கட்டணம்: ₹10\n" +
                   "📅 இப்போதே பதிவு செய்ய: /booking";
        }
        return "🌸 **Pushparchana Guide:**\n\n" +
               "**Requirements:**\n" +
               "• 108 Tulasi leaves or flowers\n" +
               "• Kumkum, Vibhuti\n" +
               "• Coconut, Banana\n\n" +
               "**Steps:**\n" +
               "1️⃣ Take bath and wear clean clothes\n" +
               "2️⃣ Calm your mind and focus\n" +
               "3️⃣ Offer each flower while chanting\n" +
               "   108 names of Lord Venkateswara\n" +
               "4️⃣ Chant 'Om Namo Narayanaya'\n" +
               "   with each offering\n" +
               "5️⃣ Finally perform Aarti with lamp\n\n" +
               "💰 Cost: ₹10\n" +
               "📅 Book now at: /booking";
    }

    private String getAbhishekamGuide(String lang) {
        if ("tamil".equals(lang)) {
            return "🪔 **திருவாபிஷேக வழிமுறை:**\n\n" +
                   "**பஞ்சாமிர்தம்:**\n" +
                   "• பால், தயிர், தேன்\n" +
                   "• நெய், சர்க்கரை\n\n" +
                   "**படிகள்:**\n" +
                   "1️⃣ பஞ்சாமிர்தத்தை தனித்தனியாக\n" +
                   "   ஊற்றி அபிஷேகம் செய்யுங்கள்\n" +
                   "2️⃣ 'ஓம் நமச்சிவாய' என்று\n" +
                   "   சொல்லிக்கொண்டு செய்யுங்கள்\n" +
                   "3️⃣ சந்தன நீர் தளிக்கவும்\n" +
                   "4️⃣ பின்னர் மலர்களால் அலங்காரம்\n" +
                   "5️⃣ தீப ஆரத்தி காட்டுங்கள்\n\n" +
                   "💰 கட்டணம்: ₹30\n" +
                   "📅 இப்போதே பதிவு செய்ய: /booking";
        }
        return "🪔 **Abhishekam Guide:**\n\n" +
               "**Panchamruta (5 sacred items):**\n" +
               "• Milk, Curd, Honey, Ghee, Sugar\n\n" +
               "**Steps:**\n" +
               "1️⃣ Pour each item separately\n" +
               "   while chanting mantras\n" +
               "2️⃣ Chant 'Om Namah Shivaya'\n" +
               "   throughout the process\n" +
               "3️⃣ Sprinkle sandalwood water\n" +
               "4️⃣ Decorate with fresh flowers\n" +
               "5️⃣ Perform lamp Aarti at the end\n\n" +
               "💰 Cost: ₹30\n" +
               "📅 Book now at: /booking";
    }

    private String getSahasranamamGuide(String lang) {
        if ("tamil".equals(lang)) {
            return "📿 **சஹஸ்ரநாம அர்ச்சனை:**\n\n" +
                   "விஷ்ணு சஹஸ்ரநாமம் — 1000 திருநாமங்கள்\n\n" +
                   "**படிகள்:**\n" +
                   "1️⃣ மனதை அமைதிப்படுத்துங்கள்\n" +
                   "2️⃣ ஒவ்வொரு நாமத்திற்கும் ஒரு மலர்\n" +
                   "   சமர்ப்பியுங்கள்\n" +
                   "3️⃣ 'ஓம் நமோ பகவதே வாசுதேவாய'\n" +
                   "   என்று தொடங்குங்கள்\n" +
                   "4️⃣ 45 நிமிட வழிபாடு\n" +
                   "5️⃣ இறுதியில் மங்கள ஆரத்தி\n\n" +
                   "💰 கட்டணம்: ₹50\n" +
                   "📅 இப்போதே பதிவு செய்ய: /booking";
        }
        return "📿 **Sahasranamam Archana:**\n\n" +
               "Vishnu Sahasranamam — 1000 divine names\n\n" +
               "**Steps:**\n" +
               "1️⃣ Calm your mind completely\n" +
               "2️⃣ Offer one flower for each name\n" +
               "3️⃣ Start with\n" +
               "   'Om Namo Bhagavate Vasudevaya'\n" +
               "4️⃣ Takes about 45 minutes\n" +
               "5️⃣ End with Mangala Aarti\n\n" +
               "💰 Cost: ₹50\n" +
               "📅 Book now at: /booking";
    }

    private String getGeneralPoojaGuide(String lang) {
        if ("tamil".equals(lang)) {
            return "🛕 **பொது பூஜை வழிகாட்டுதல்:**\n\n" +
                   "எந்த பூஜையிலும் இந்த அடிப்படைகள்\n" +
                   "மிக முக்கியம்:\n\n" +
                   "✅ **சுத்தம்** — உடல் மற்றும் மன சுத்தம்\n" +
                   "✅ **பக்தி** — முழு மனதுடன் வழிபாடு\n" +
                   "✅ **நம்பிக்கை** — கடவுளில் நம்பிக்கை\n" +
                   "✅ **அர்ப்பணிப்பு** — முழுவதும் கொடுங்கள்\n\n" +
                   "எந்த குறிப்பிட்ட பூஜை பற்றி\n" +
                   "தெரிந்துகொள்ள விரும்புகிறீர்கள்?";
        }
        return "🛕 **General Pooja Guide:**\n\n" +
               "These fundamentals apply to all poojas:\n\n" +
               "✅ **Cleanliness** — Body and mind purity\n" +
               "✅ **Devotion** — Whole-hearted worship\n" +
               "✅ **Faith** — Complete trust in God\n" +
               "✅ **Surrender** — Offer everything to Him\n\n" +
               "Which specific pooja would you\n" +
               "like to know about?";
    }

    // ── EMOTIONAL ────────────────────────────────────
    private String getEmotionalResponse(String lang) {
        String[] slokams = {
            "கர्मण्येवाधिकारस्ते मा फलेषु कदाचन।\n" +
            "மா கர்மபல ஹேதுர் பூர்\n" +
            "மா தே சங்கோஸ்த்வ கர்மணி।\n\n" +
            "— பகவத் கீதை 2:47",

            "யதா யதா ஹி தர்மஸ்ய\n" +
            "க்லானிர் பவதி பாரத।\n" +
            "அப்யுத்தானம் அதர்மஸ்ய\n" +
            "ததாத்மானம் ஸ்ருஜாம்யஹம்।\n\n" +
            "— பகவத் கீதை 4:7"
        };

        String randomSlokam = slokams[
            new Random().nextInt(slokams.length)];

        if ("tamil".equals(lang)) {
            return "🙏 அன்பு பக்தரே,\n\n" +
                   "உங்கள் வலியை புரிந்துகொள்கிறேன். " +
                   "ஸ்ரீ வெங்கடேஸ்வர பெருமாள் " +
                   "எப்போதும் உங்களுடன் இருக்கிறார்.\n\n" +
                   "**இந்த ஸ்லோகம் உங்களுக்கு:**\n\n" +
                   "📿 *" + randomSlokam + "*\n\n" +
                   "**அர்த்தம்:** உங்கள் கடமையை " +
                   "செய்யுங்கள், பலனை கடவுளிடம் " +
                   "விட்டுவிடுங்கள். அவர் எல்லாவற்றையும் " +
                   "சரியாக செய்வார்.\n\n" +
                   "💙 நீங்கள் தனியில்லை. " +
                   "கோவிலுக்கு வாருங்கள், " +
                   "மனம் நிம்மதி பெறும்.\n\n" +
                   "பூஜை செய்ய விரும்புகிறீர்களா? " +
                   "நான் உதவுகிறேன் 🌸";
        }
        return "🙏 Dear devotee,\n\n" +
               "I understand your pain. " +
               "Lord Venkateswara is always " +
               "with you.\n\n" +
               "**A sacred slokam for you:**\n\n" +
               "📿 *" + randomSlokam + "*\n\n" +
               "**Meaning:** Do your duty and " +
               "surrender the results to God. " +
               "He will take care of everything.\n\n" +
               "💙 You are never alone. " +
               "Come to the temple, " +
               "your mind will find peace.\n\n" +
               "Would you like to book a " +
               "pooja? I can help you 🌸";
    }

    // ── DONATION ─────────────────────────────────────
    private String getDonationResponse(String lang) {
        if ("tamil".equals(lang)) {
            return "🙏 **தானம் — மிகவும் புண்ணியமான செயல்!**\n\n" +
                   "**தான வகைகள்:**\n\n" +
                   "🍚 **அன்னதானம்** — ₹100 முதல்\n" +
                   "   ஏழை மக்களுக்கு உணவு வழங்குதல்\n\n" +
                   "🛕 **கோவில் நிதி** — ₹500 முதல்\n" +
                   "   கோவில் பராமரிப்பு மற்றும் வளர்ச்சி\n\n" +
                   "🕉️ **சிறப்பு பூஜை** — ₹1000 முதல்\n" +
                   "   உங்கள் பெயரில் விசேஷ பூஜை\n\n" +
                   "🐄 **கோதானம்** — ₹2000 முதல்\n" +
                   "   புனித பசுவிற்கு உதவி\n\n" +
                   "📱 **இப்போதே தானம் செய்ய:**\n" +
                   "[தானம் செய்ய இங்கே கிளிக் செய்யவும்](/donation)\n\n" +
                   "உங்கள் தானம் பல உயிர்களை\n" +
                   "ஆசீர்வதிக்கும்! 🙏";
        }
        return "🙏 **Donation — A Noble Act!**\n\n" +
               "**Donation Types:**\n\n" +
               "🍚 **Annadhanam** — From ₹100\n" +
               "   Feed the poor and needy\n\n" +
               "🛕 **Temple Fund** — From ₹500\n" +
               "   Temple maintenance & development\n\n" +
               "🕉️ **Special Pooja** — From ₹1000\n" +
               "   Special pooja in your name\n\n" +
               "🐄 **Cow Donation** — From ₹2000\n" +
               "   Support the sacred cow\n\n" +
               "📱 **Donate now:**\n" +
               "[Click here to donate](/donation)\n\n" +
               "Your donation will bless\n" +
               "many lives! 🙏";
    }

    // ── BOOKING ──────────────────────────────────────
    private String getBookingResponse(String lang) {
        if ("tamil".equals(lang)) {
            return "📿 **அர்ச்சனை பதிவு செய்வது எப்படி?**\n\n" +
                   "**படிகள்:**\n\n" +
                   "1️⃣ [இங்கே கிளிக் செய்யவும்](/booking)\n" +
                   "2️⃣ உங்கள் பெயர் மற்றும்\n" +
                   "   கோத்திரம் உள்ளிடுங்கள்\n" +
                   "3️⃣ நட்சத்திரம் தேர்வு செய்யுங்கள்\n" +
                   "4️⃣ அர்ச்சனை வகை தேர்வு செய்யுங்கள்:\n\n" +
                   "   🌸 புஷ்பார்ச்சனை — ₹10\n" +
                   "   🔴 குங்குமார்ச்சனை — ₹20\n" +
                   "   🪔 அபிஷேகம் — ₹30\n" +
                   "   📿 சஹஸ்ரநாமம் — ₹50\n" +
                   "   🔥 கணபதி ஹோமம் — ₹70\n\n" +
                   "5️⃣ நேர அட்டவணை தேர்வு செய்யுங்கள்\n" +
                   "6️⃣ கட்டணம் செலுத்துங்கள்\n" +
                   "7️⃣ QR டிக்கெட் பெறுங்கள்!\n\n" +
                   "👉 [இப்போதே பதிவு செய்யுங்கள்](/booking)";
        }
        return "📿 **How to Book Archana?**\n\n" +
               "**Steps:**\n\n" +
               "1️⃣ [Click here to start](/booking)\n" +
               "2️⃣ Enter your name and gothram\n" +
               "3️⃣ Select your birth star (nakshatram)\n" +
               "4️⃣ Choose archana type:\n\n" +
               "   🌸 Pushparchana — ₹10\n" +
               "   🔴 Kumkumarchana — ₹20\n" +
               "   🪔 Abhishekam — ₹30\n" +
               "   📿 Sahasranamam — ₹50\n" +
               "   🔥 Ganapathi Homam — ₹70\n\n" +
               "5️⃣ Select time slot\n" +
               "6️⃣ Complete payment\n" +
               "7️⃣ Get your QR ticket!\n\n" +
               "👉 [Book now](/booking)";
    }

    // ── INFO ─────────────────────────────────────────
    private String getInfoResponse(
            String message, String lang) {

        String m = message.toLowerCase();

        if (containsAny(m, new String[]{
            "timing", "time", "open", "close",
            "hours", "நேரம்", "திறக்கும்"})) {
            return getTimingInfo(lang);
        }

        if (containsAny(m, new String[]{
            "price", "cost", "fee", "charge",
            "how much", "விலை", "கட்டணம்"})) {
            return getPriceInfo(lang);
        }

        return getGeneralInfo(lang);
    }

    private String getTimingInfo(String lang) {
        if ("tamil".equals(lang)) {
            return "🕐 **கோவில் நேரங்கள்:**\n\n" +
                   "**காலை:**\n" +
                   "⏰ 06:00 AM — கோவில் திறப்பு\n" +
                   "⏰ 06:00 - 07:00 AM — சுப்ரபாதம்\n" +
                   "⏰ 08:00 - 09:00 AM — காலை பூஜை\n\n" +
                   "**மதியம்:**\n" +
                   "⏰ 10:00 - 11:00 AM — முற்பகல் பூஜை\n" +
                   "⏰ 12:00 - 01:00 PM — நண்பகல் பூஜை\n\n" +
                   "**மாலை:**\n" +
                   "⏰ 04:00 - 05:00 PM — மாலை பூஜை\n" +
                   "⏰ 06:00 - 07:00 PM — சந்தியா வழிபாடு\n\n" +
                   "**இரவு:**\n" +
                   "⏰ 08:00 - 09:00 PM — இரவு பூஜை\n" +
                   "⏰ 09:00 PM — கோவில் மூடல்\n\n" +
                   "📅 [நேரம் பதிவு செய்ய](/booking)";
        }
        return "🕐 **Temple Timings:**\n\n" +
               "**Morning:**\n" +
               "⏰ 06:00 AM — Temple Opens\n" +
               "⏰ 06:00 - 07:00 AM — Suprabhatam\n" +
               "⏰ 08:00 - 09:00 AM — Morning Pooja\n\n" +
               "**Afternoon:**\n" +
               "⏰ 10:00 - 11:00 AM — Forenoon Pooja\n" +
               "⏰ 12:00 - 01:00 PM — Noon Pooja\n\n" +
               "**Evening:**\n" +
               "⏰ 04:00 - 05:00 PM — Evening Pooja\n" +
               "⏰ 06:00 - 07:00 PM — Sandhya Pooja\n\n" +
               "**Night:**\n" +
               "⏰ 08:00 - 09:00 PM — Night Pooja\n" +
               "⏰ 09:00 PM — Temple Closes\n\n" +
               "📅 [Book a time slot](/booking)";
    }

    private String getPriceInfo(String lang) {
        if ("tamil".equals(lang)) {
            return "💰 **அர்ச்சனை கட்டணங்கள்:**\n\n" +
                   "🌸 புஷ்பார்ச்சனை — ₹10\n" +
                   "🔴 குங்குமார்ச்சனை — ₹20\n" +
                   "🪔 அபிஷேகம் — ₹30\n" +
                   "📿 சஹஸ்ரநாமம் — ₹50\n" +
                   "🔥 கணபதி ஹோமம் — ₹70\n\n" +
                   "**தானம்:**\n" +
                   "🍚 அன்னதானம் — ₹100+\n" +
                   "🛕 கோவில் நிதி — ₹500+\n" +
                   "🕉️ சிறப்பு பூஜை — ₹1000+\n\n" +
                   "📅 [இப்போதே பதிவு செய்யுங்கள்](/booking)";
        }
        return "💰 **Archana Charges:**\n\n" +
               "🌸 Pushparchana — ₹10\n" +
               "🔴 Kumkumarchana — ₹20\n" +
               "🪔 Abhishekam — ₹30\n" +
               "📿 Sahasranamam — ₹50\n" +
               "🔥 Ganapathi Homam — ₹70\n\n" +
               "**Donations:**\n" +
               "🍚 Annadhanam — ₹100+\n" +
               "🛕 Temple Fund — ₹500+\n" +
               "🕉️ Special Pooja — ₹1000+\n\n" +
               "📅 [Book now](/booking)";
    }

    private String getGeneralInfo(String lang) {
        if ("tamil".equals(lang)) {
            return "🛕 **ஸ்ரீ வெங்கடேஸ்வர கோவில்**\n\n" +
                   "📍 **இடம்:** தமிழ்நாடு, இந்தியா\n" +
                   "📞 **தொடர்பு:** 044-XXXXXXXX\n" +
                   "🌐 **இணையதளம்:** localhost:8080\n\n" +
                   "**சேவைகள்:**\n" +
                   "✅ ஆன்லைன் அர்ச்சனை பதிவு\n" +
                   "✅ QR டிக்கெட் வழங்கல்\n" +
                   "✅ AI குரல் அர்ச்சனை\n" +
                   "✅ ஆன்லைன் தானம்\n\n" +
                   "வேறு ஏதாவது தெரிந்துகொள்ள\n" +
                   "விரும்புகிறீர்களா?";
        }
        return "🛕 **Sri Venkateswara Temple**\n\n" +
               "📍 **Location:** Tamil Nadu, India\n" +
               "📞 **Contact:** 044-XXXXXXXX\n" +
               "🌐 **Website:** localhost:8080\n\n" +
               "**Services:**\n" +
               "✅ Online Archana Booking\n" +
               "✅ QR Ticket Generation\n" +
               "✅ AI Voice Archana\n" +
               "✅ Online Donation\n\n" +
               "Would you like to know\n" +
               "anything else?";
    }

    // ── GENERAL ──────────────────────────────────────
    private String getGeneralResponse(String lang) {
        if ("tamil".equals(lang)) {
            return "🙏 வணக்கம்! நான் உங்களுக்கு\n" +
                   "எந்த விஷயத்திலும் உதவுவேன்:\n\n" +
                   "📿 **பூஜை வழிகாட்டுதல்**\n" +
                   "   'pushparchana எப்படி செய்வது'\n\n" +
                   "💙 **ஆன்மீக ஆறுதல்**\n" +
                   "   'நான் மிகவும் கஷ்டப்படுகிறேன்'\n\n" +
                   "📅 **பதிவு உதவி**\n" +
                   "   'அர்ச்சனை பதிவு செய்ய விரும்புகிறேன்'\n\n" +
                   "🙏 **தானம்**\n" +
                   "   'donate செய்ய விரும்புகிறேன்'\n\n" +
                   "🕐 **கோவில் தகவல்**\n" +
                   "   'கோவில் நேரம் என்ன'\n\n" +
                   "என்ன கேட்கலாம்?";
        }
        return "🙏 Vanakkam! I can help you with:\n\n" +
               "📿 **Pooja Guidance**\n" +
               "   'how to do pushparchana'\n\n" +
               "💙 **Spiritual Comfort**\n" +
               "   'I am feeling stressed'\n\n" +
               "📅 **Booking Help**\n" +
               "   'I want to book archana'\n\n" +
               "🙏 **Donation**\n" +
               "   'I want to donate'\n\n" +
               "🕐 **Temple Info**\n" +
               "   'what are temple timings'\n\n" +
               "What would you like to know?";
    }

    // ════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════
    private boolean containsAny(
            String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String detectLanguage(
            String message, String preferred) {

        // Check for Tamil characters
        for (char c : message.toCharArray()) {
            if (c >= 0x0B80 && c <= 0x0BFF) {
                return "tamil";
            }
        }

        // Check Tamil keywords in English
        String m = message.toLowerCase();
        if (containsAny(m, new String[]{
            "vanakkam", "pooja", "kovil",
            "archana", "nakshatram"})) {
            return "tamil";
        }

        return preferred != null ? preferred : "english";
    }

    private List<String> getSuggestions(String intent) {
        List<String> suggestions = new ArrayList<>();
        switch (intent) {
            case "GUIDANCE":
                suggestions.add("How to do Abhishekam?");
                suggestions.add("Sahasranamam guide");
                suggestions.add("Book a pooja");
                break;
            case "EMOTIONAL":
                suggestions.add("Book a calming pooja");
                suggestions.add("Temple timings");
                suggestions.add("I want to donate");
                break;
            case "DONATION":
                suggestions.add("Donate now");
                suggestions.add("Annadhanam details");
                suggestions.add("Temple Fund info");
                break;
            case "BOOKING":
                suggestions.add("Book Pushparchana ₹10");
                suggestions.add("Book Abhishekam ₹30");
                suggestions.add("View all archana types");
                break;
            case "INFO":
                suggestions.add("Temple timings");
                suggestions.add("Archana prices");
                suggestions.add("Book a pooja");
                break;
            default:
                suggestions.add("Book Archana");
                suggestions.add("Make a donation");
                suggestions.add("Temple timings");
        }
        return suggestions;
    }

    // ════════════════════════════════════════════════
    // GET CHAT HISTORY
    // ════════════════════════════════════════════════
    public List<ChatMessage> getChatHistory(
            String sessionId) {
        return chatRepo
            .findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}