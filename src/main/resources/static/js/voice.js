// ============================================================
// AI VOICE ARCHANA — Fish Audio TTS (Voice ID: db857de9b1be4f9cb53799daaa69218e)
// ============================================================

var currentBookingData   = null;
var currentArchanaScript = null;
var currentQuickScript   = null;
var isSpeaking           = false;
var currentAudio         = null;

// ── MAIN FUNCTION: Play Full Priest Archana ──────────────────
function playArchanaVoice() {
    if (!currentBookingData && !currentArchanaScript) {
        alert('Please verify a booking first.');
        return;
    }
    var script = buildPriestScript();
    speakPriestStyle(script, 'playArchanaBtn');
}

// ── QUICK ANNOUNCEMENT ───────────────────────────────────────
function playQuickAnnouncement() {
    if (!currentBookingData) {
        alert('Please verify a booking first.');
        return;
    }
    var script = buildQuickScript();
    speakPriestStyle(script, 'quickAnnounceBtn');
}

// ── STOP VOICE ───────────────────────────────────────────────
function stopVoice() {
    if (currentAudio) {
        currentAudio.pause();
        currentAudio.currentTime = 0;
        currentAudio = null;
    }
    isSpeaking = false;
    resetAllButtons();
}

// ════════════════════════════════════════════════════════════
// BUILD PRIEST ARCHANA SCRIPT
// ════════════════════════════════════════════════════════════
function buildPriestScript() {

    if (!currentBookingData) {
        return currentArchanaScript || 'Om Namah. Archana samarpanam.';
    }

    var name    = currentBookingData.devoteeName || '';
    var gothram = currentBookingData.gothram     || '';
    var naksha  = currentBookingData.nakshatram  || '';
    var archana = currentBookingData.archanaType || '';

    return (
        'Om. ' +
        'Shubham Karoti Kalyanam. ' +
        'Arogyam. Dhana Sampada. ' +
        'Shatru Buddhi Vinashaya. ' +
        'Deepa Jyotir Namostute. ' +

        'Adiyargale. ' +

        name + ' avargalukku. ' +
        gothram + ' gothram. ' +
        naksha + ' nakshatram. ' +

        getArchanaLines(archana) +

        'Sarva Mangala Prapthirastu. ' +
        'Sarva Vigna Nivaranam. ' +
        'Ayu. Arogyam. Aishwaryam. ' +
        'Dheerga Ayushu Prapthirastu. ' +
        'Puthra. Pautra. Abhivruddhi Rastu. ' +

        'Om Shanti. ' +
        'Shanti. ' +
        'Shantihi. ' +

        'Jai Sri Venkateswara. ' +
        'Govinda. ' +
        'Govinda. ' +
        'Govinda.'
    );
}

// ── ARCHANA TYPE SPECIFIC LINES ──────────────────────────────
function getArchanaLines(archanaType) {
    switch(archanaType) {
        case 'Pushparchana':
            return (
                'Pushpa Archana samarpanam. ' +
                'Om Namo Narayanaya. ' +
                'Satha Thulasi Pushpam Samarpayami. '
            );
        case 'Kumkumarchana':
            return (
                'Kumkuma Archana samarpanam. ' +
                'Om Shakti Namaha. ' +
                'Kumkumam Samarpayami. ' +
                'Devi Prasadam Prapthirastu. '
            );
        case 'Abhishekam':
            return (
                'Abhishekam samarpanam. ' +
                'Om Namah Shivaya. ' +
                'Jala Abhishekam. Dugdha Abhishekam. ' +
                'Panchamrutha Abhishekam Samarpayami. '
            );
        case 'Sahasranamam':
            return (
                'Sahasranama Archana samarpanam. ' +
                'Om Namo Bhagavate Vasudevaya. ' +
                'Sahasra Namam. Sahasra Pushpam. ' +
                'Samarpayami. '
            );
        case 'Ganapathi Homam':
            return (
                'Ganapathi Homam samarpanam. ' +
                'Om Gam Ganapataye Namaha. ' +
                'Swaha. Swaha. Poornahuti Samarpayami. ' +
                'Vigna Nivaranam. Vigna Nashanam. '
            );
        default:
            return (
                'Archana samarpanam. ' +
                'Om Namo Narayanaya. '
            );
    }
}

// ── QUICK SCRIPT ─────────────────────────────────────────────
function buildQuickScript() {
    if (!currentBookingData) return 'Om Namah. Archana confirmed.';
    var b = currentBookingData;
    return (
        'Om Namah. ' +
        (b.devoteeName || '') + ' avargalukku. ' +
        (b.gothram     || '') + ' gothram. ' +
        (b.nakshatram  || '') + ' nakshatram. ' +
        (b.archanaType || '') + ' booking confirmed. ' +
        'Sarva Mangala Prapthirastu. ' +
        'Govinda. Govinda.'
    );
}

// ════════════════════════════════════════════════════════════
// CORE SPEAKING ENGINE — Fish Audio TTS API
// ════════════════════════════════════════════════════════════
function speakPriestStyle(fullText, btnId) {

    // Stop any currently playing audio
    if (currentAudio) {
        currentAudio.pause();
        currentAudio = null;
    }

    isSpeaking = true;

    var btn = document.getElementById(btnId);
    if (btn) {
        btn.textContent   = '🔊 Generating AI Voice...';
        btn.disabled      = true;
        btn.style.opacity = '0.7';
    }

    var display = document.getElementById('voiceDisplay');
    if (display) {
        display.style.display = 'block';
        display.innerHTML =
            '<div style="color:#FFD700; font-weight:600; margin-bottom:8px;">' +
            '🎵 Archana being chanted:</div>' +
            '<div style="font-style:italic; line-height:2; color:#ffe066;">' +
            fullText +
            '</div>' +
            '<div style="color:#aaa; margin-top:8px;">⏳ Connecting to Fish AI Priest Voice...</div>';
    }

    // Call Spring Boot backend → Fish Audio API
    fetch('/api/fish-tts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: fullText })
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Server error: ' + response.status);
        }
        return response.blob();
    })
    .then(function(audioBlob) {
        var audioUrl = URL.createObjectURL(audioBlob);
        currentAudio = new Audio(audioUrl);

        if (btn) {
            btn.textContent   = '🔊 Chanting Archana...';
            btn.disabled      = true;
            btn.style.opacity = '0.7';
        }

        if (display) {
            display.innerHTML =
                '<div style="color:#FFD700; font-weight:600; margin-bottom:8px;">' +
                '🎵 Archana being chanted:</div>' +
                '<div style="font-style:italic; line-height:2; color:#ffe066;">' +
                fullText +
                '</div>' +
                '<div style="color:#4ade80; margin-top:8px;">🔊 Fish AI Priest Voice playing...</div>';
        }

        currentAudio.play();

        currentAudio.onended = function() {
            isSpeaking = false;
            URL.revokeObjectURL(audioUrl);
            currentAudio = null;

            if (btn) {
                btn.textContent   = '🔊 Play Archana Again';
                btn.disabled      = false;
                btn.style.opacity = '1';
            }
            if (display) {
                display.innerHTML +=
                    '<div style="color:#4ade80; margin-top:12px; font-weight:600;">' +
                    '✅ Archana completed. Sarva Mangala Prapthirastu. 🙏</div>';
            }
        };

        currentAudio.onerror = function() {
            isSpeaking = false;
            currentAudio = null;
            if (btn) {
                btn.textContent   = '🔊 Retry';
                btn.disabled      = false;
                btn.style.opacity = '1';
            }
            if (display) {
                display.innerHTML +=
                    '<div style="color:red; margin-top:8px;">❌ Audio playback failed.</div>';
            }
        };
    })
    .catch(function(err) {
        console.error('Fish Audio TTS error:', err);
        isSpeaking = false;
        currentAudio = null;

        if (btn) {
            btn.textContent   = '🔊 Retry';
            btn.disabled      = false;
            btn.style.opacity = '1';
        }
        if (display) {
            display.innerHTML +=
                '<div style="color:red; margin-top:8px;">❌ Voice generation failed: ' +
                err.message + '</div>';
        }

        // Fallback to browser voice if Fish Audio fails
        fallbackBrowserVoice(fullText, btnId);
    });
}

// ════════════════════════════════════════════════════════════
// FALLBACK — Browser Voice if Fish Audio API fails
// ════════════════════════════════════════════════════════════
function fallbackBrowserVoice(fullText, btnId) {

    if (!('speechSynthesis' in window)) return;

    var btn = document.getElementById(btnId);
    if (btn) {
        btn.textContent   = '🔊 Using Browser Voice (Fallback)...';
        btn.disabled      = true;
        btn.style.opacity = '0.7';
    }

    window.speechSynthesis.cancel();

    function doSpeak() {
        var voices = window.speechSynthesis.getVoices();

        var selectedVoice =
            voices.find(function(v) { return v.lang === 'en-IN'; }) ||
            voices.find(function(v) { return v.lang === 'en-GB'; }) ||
            voices.find(function(v) { return v.lang.startsWith('en'); });

        var utterance       = new SpeechSynthesisUtterance(fullText);
        utterance.rate      = 0.65;
        utterance.pitch     = 0.7;
        utterance.volume    = 1.0;

        if (selectedVoice) utterance.voice = selectedVoice;

        utterance.onend = function() {
            isSpeaking = false;
            if (btn) {
                btn.textContent   = '🔊 Play Archana Again';
                btn.disabled      = false;
                btn.style.opacity = '1';
            }
        };

        window.speechSynthesis.speak(utterance);
    }

    if (window.speechSynthesis.getVoices().length > 0) {
        doSpeak();
    } else {
        window.speechSynthesis.onvoiceschanged = function() {
            window.speechSynthesis.onvoiceschanged = null;
            doSpeak();
        };
        setTimeout(doSpeak, 500);
    }
}

// ── HELPER: Reset all voice buttons ──────────────────────────
function resetAllButtons() {
    var btns = ['playArchanaBtn', 'quickAnnounceBtn'];
    btns.forEach(function(id) {
        var btn = document.getElementById(id);
        if (btn) {
            btn.disabled      = false;
            btn.style.opacity = '1';
            btn.textContent   = id === 'playArchanaBtn'
                ? '🎙️ Play Full Priest Archana Voice'
                : '🔔 Play Quick Announcement';
        }
    });
}