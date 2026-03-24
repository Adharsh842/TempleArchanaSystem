package com.temple.service;

import org.springframework.stereotype.Service;

@Service
public class ArchanaVoiceService {

    /**
     * Builds a priest-style archana script (Iyer style)
     * This text is sent to the browser which speaks it using Web Speech API
     */
    public String buildArchanaScript(String devoteeName,
                                      String gothram,
                                      String nakshatram,
                                      String archanaType) {

        // Build the full archana chant text
        StringBuilder archana = new StringBuilder();

        archana.append("Om... ");
        archana.append("Shubham Karoti Kalyanam. ");
        archana.append("Arogya Dhana Sampada. ");
        archana.append("Shatru Buddhi Vinashaya. ");
        archana.append("Deepa Jyotir Namostute. ");
        archana.append("Om... ");

        // Devotee announcement
        archana.append(devoteeName).append(" avargaluku, ");
        archana.append(gothram).append(" gothram, ");
        archana.append(nakshatram).append(" nakshatram, ");

        // Archana type specific chant
        archana.append(getArchanaChant(archanaType));

        // Blessing
        archana.append("Sarva Mangala Prapthirastu. ");
        archana.append("Sarva Vigna Nivaranam. ");
        archana.append("Ayu Arogyam Aishwaryam. ");
        archana.append("Dheerga Ayushu Prapthirastu. ");
        archana.append("Om Shanti. Shanti. Shantihi. ");
        archana.append("Jai Sri Venkateswara! ");
        archana.append("Govinda! Govinda! Govinda!");

        return archana.toString();
    }

    /**
     * Returns specific chant based on archana type
     */
    private String getArchanaChant(String archanaType) {
        if (archanaType == null) return "Archana samarpanam. ";

        return switch (archanaType.trim()) {
            case "Pushparchana" ->
                "Pushpa archana samarpanam. " +
                "Om Namo Narayanaya. " +
                "Pushpam samarpayami. ";

            case "Kumkumarchana" ->
                "Kumkuma archana samarpanam. " +
                "Om Shakti Namaha. " +
                "Kumkumam samarpayami. ";

            case "Abhishekam" ->
                "Abhishekam samarpanam. " +
                "Om Namah Shivaya. " +
                "Jalabhishekam samarpayami. ";

            case "Sahasranamam" ->
                "Sahasranama archana samarpanam. " +
                "Om Namo Bhagavate Vasudevaya. " +
                "Sahasra pushpam samarpayami. ";

            case "Ganapathi Homam" ->
            "Shuklam Baradharam Vishnum. " +
            "Shashi Varnam Chaturbhujam. " +
            "Prasanna Vadanam Dhyayet." +
            "Sarva Vighnopashantaye." +

            "Om Gam Ganapataye Namaha. " +
            "Avahayami Sthapayami. " +

            "Om Ganapataye Namaha. " +
            "Asanam Samarpayami. " +
            "Arghyam Samarpayami. " +
            "Paadyam Samarpayami. " +

            "Om Ganapataye Namaha. " +
            "Snanam Samarpayami. " +

            "Om Ganapataye Namaha. " +
            "Vastram Samarpayami. " +
            "Gandham Samarpayami. " +
            "Pushpam Samarpayami. " +

            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +
            "Om Gam Ganapataye Namaha. " +

            "Om Ekadantaya Vidmahe. " +
            "Vakratundaya Dheemahi. " +
            "Tanno Dantih Prachodayat. " +

            "Om Namaste Ganapataye. " +
            "Tvameva Pratyaksham Tatvamasi. " +
            "Tvameva Kevalam Kartasi. " +
            "Tvameva Kevalam Dhartasi. " +
            "Tvameva Kevalam Hartasi. " +
            "Tvameva Sarvam Khalvidam Brahmasi. " +
            "Tvam Sakshad Atmasi Nityam. " +

            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +
            "Om Gam Ganapataye Swaha. " +

            "Om Shreem Hreem Kleem Glaum. " +
            "Gam Ganapataye. " +
            "Vara Varada. " +
            "Sarvajanam Me Vashamanaya Swaha." +

            "Vakratunda Mahakaya. " +
            "Suryakoti Samaprabha. " +
            "Nirvighnam Kurume Deva. " +
            "Sarva Karyeshu Sarvada. " +

            "Om Gan Ganapataye Namaha. " +
            "Jaya Ganesh Jaya Ganesh. " +
            "Jaya Ganesh Deva. " +

            "Kayena Vacha Manasendriyairva. " +
            "Buddhyatmana Va Prakriteh Swabhavat. " +
            "Karomi Yadyat Sakalam Parasmai. " +
            "Narayanayeti Samarpayami. ";
                 
              

            default ->
                "Archana samarpanam. " +
                "Om Namo Narayanaya. ";
        };
    }

    /**
     * Builds a SHORT announcement for quick verification
     */
    public String buildQuickAnnouncement(String devoteeName,
                                          String gothram,
                                          String nakshatram,
                                          String archanaType,
                                          String timeSlot) {
        return "Om Namah. " +
               devoteeName + " avargaluku, " +
               gothram + " gothram, " +
               nakshatram + " nakshatram, " +
               archanaType + " booking confirmed. " +
               "Time slot: " + timeSlot + ". " +
               "Sarva Mangala Prapthirastu. " +
               "Govinda Govinda!";
    }
}