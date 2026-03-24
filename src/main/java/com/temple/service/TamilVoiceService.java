package com.temple.service;

import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TamilVoiceService {

    /**
     * Builds Google TTS URL for Tamil text
     * This URL returns an MP3 audio file
     */
    public String buildTamilAudioUrl(String text) {
        try {
            String encoded = URLEncoder.encode(text,
                StandardCharsets.UTF_8);
            return "https://translate.google.com/translate_tts" +
                   "?ie=UTF-8" +
                   "&q=" + encoded +
                   "&tl=ta" +     // ta = Tamil language code
                   "&client=tw-ob";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Builds Tamil archana text for the devotee
     */
    public String buildTamilArchanaText(
            String devoteeName,
            String gothram,
            String nakshatram,
            String archanaType) {

        StringBuilder sb = new StringBuilder();

        // Opening mantra
        sb.append("ஓம் நமச்சிவாய. ");
        sb.append("சுபம் கரோதி கல்யாணம். ");
        sb.append("ஆரோக்யம் தன சம்பதா. ");

        // Devotee announcement
        sb.append(devoteeName).append(" அவர்களுக்கு, ");
        sb.append(gothram).append(" கோத்திரம், ");
        sb.append(nakshatram).append(" நட்சத்திரம், ");

        // Archana type
        sb.append(archanaType).append(" சமர்ப்பணம். ");

        // Blessings
        sb.append("சர்வ மங்கள பிராப்திரஸ்து. ");
        sb.append("ஆயு ஆரோக்யம் ஐஸ்வர்யம். ");
        sb.append("தீர்க்க ஆயுசு பிராப்திரஸ்து. ");
        sb.append("ஜெய் ஸ்ரீ வெங்கடேஸ்வரா. ");
        sb.append("கோவிந்தா கோவிந்தா.");

        return sb.toString();
    }

    /**
     * Builds Tamil text in English letters (Transliteration)
     * This works with any English TTS voice
     * and sounds like Tamil when spoken
     */
    public String buildTamilTransliterationText(
            String devoteeName,
            String gothram,
            String nakshatram,
            String archanaType) {

        return
            "Om......... " +
            "Nama Shivaya......... " +
            "Subham Karodi Kalyanam......... " +
            "Arokyam Thana Sambatha......... " +

            // Devotee
            devoteeName + " avargalukku.............. " +
            gothram + " gothiram.............. " +
            nakshatram + " natchathiram............... " +

            // Archana
            getTamilArchanaChant(archanaType) +

            // Blessings
            "Sarva Mangala Piraapthirastu.............. " +
            "Aayu Arokyam Aishwaryam.............. " +
            "Dheerka Aayushu Piraapthirastu............... " +
            "Om Shaanthi......... " +
            "Shaanthi......... " +
            "Shaanthihi.................... " +
            "Jai Sri Venkadaeshwara................ " +
            "Govindaa......... " +
            "Govindaa......... " +
            "Govindaa";
    }

    private String getTamilArchanaChant(String archanaType) {
        if (archanaType == null) return "Archana samarppanam.......... ";

        return switch (archanaType.trim()) {
            case "Pushparchana" ->
                "Pushpa Archana samarppanam.............. " +
                "Om Namo Narayanaya............... " +
                "Pushpam samarpayami............... ";

            case "Kumkumarchana" ->
                "Kumkuma Archana samarppanam.............. " +
                "Om Sakthi Namaha.............. " +
                "Kumkumam samarpayami............... ";

            case "Abhishekam" ->
                "Abishekam samarppanam............... " +
                "Om Namah Shivaya................. " +
                "Panchaamirutha Abishekam samarpayami................ ";

            case "Sahasranamam" ->
                "Sahasranama Archana samarppanam................. " +
                "Om Namo Bhagavathe Vasudevaaya.................. " +
                "Sahasra Pushpam samarpayami.................. ";

            case "Ganapathi Homam" ->
                "Ganapathi Homam samarppanam..................... " +
                "Om Gam Ganapataye Namaha...................... " +
                "Poornaahudhi samarpayami..................... ";

            default ->
                "Archana samarppanam................. " +
                "Om Namo Narayanaya................... ";
        };
    }
}