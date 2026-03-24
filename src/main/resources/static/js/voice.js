// ============================================================
// AI VOICE ARCHANA — Priest Style Tuned Web Speech API
// ============================================================

var currentBookingData   = null;
var currentArchanaScript = null;
var currentQuickScript   = null;
var isSpeaking           = false;

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
    if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        isSpeaking = false;
    }
    resetAllButtons();
}

// ════════════════════════════════════════════════════════════
// BUILD PRIEST ARCHANA SCRIPT
// Pauses using commas and periods make it sound ceremonial
// ════════════════════════════════════════════════════════════
function buildPriestScript() {

    if (!currentBookingData) {
        return currentArchanaScript || 'Om Namah. Archana samarpanam.';
    }

    var name     = currentBookingData.devoteeName || '';
    var gothram  = currentBookingData.gothram     || '';
    var naksha   = currentBookingData.nakshatram  || '';
    var archana  = currentBookingData.archanaType || '';
    var slot     = currentBookingData.timeSlot    || '';

    // Commas and full stops create natural pauses in speech
    return (
        'Om............ ' +
        'Shubham Karoti Kalyanam. ' +
        'Arogyam. Dhana Sampada. ' +
        'Shatru Buddhi Vinashaya. ' +
        'Deepa Jyotir Namostute.............. ' +

        'Adiyargale............... ' +

        // Devotee announcement — spoken slowly
        name + ' avargalukku.............. ' +
        gothram + ' gothram.............. ' +
        naksha + ' nakshatram............... ' +

        // Archana specific
        getArchanaLines(archana) +

        // Final blessings
        'Sarva Mangala Prapthirastu............... ' +
        'Sarva Vigna Nivaranam.............. ' +
        'Ayu. Arogyam. Aishwaryam............. ' +
        'Dheerga Ayushu Prapthirastu................ ' +
        'Puthra. Pautra. Abhivruddhi Rastu............. ' +

        'Om Shanti.............. ' +
        'Shanti............... ' +
        'Shantihi........................ ' +

        'Jai Sri Venkateswara.......................... ' +
        'Govinda............. ' +
        'Govinda............. ' +
        'Govinda'
    );
}

// ── ARCHANA TYPE SPECIFIC LINES ──────────────────────────────
function getArchanaLines(archanaType) {
    switch(archanaType) {
        case 'Pushparchana':
            return (
                'Pushpa Archana samarpanam............. ' +
                'Om Namo Narayanaya.............. ' +
                'Satha Thulasi Pushpam Samarpayami.............. '
            );
        case 'Kumkumarchana':
            return (
                'Kumkuma Archana samarpanam............. ' +
                'Om Shakti Namaha.............. ' +
                'Kumkumam Samarpayami............. ' +
                'Devi Prasadam Prapthirastu............... '
            );
        case 'Abhishekam':
            return (
                'Abhishekam samarpanam.............. ' +
                'Om Namah Shivaya................ ' +
                'Jala Abhishekam. Dugdha Abhishekam.............. ' +
                'Panchamrutha Abhishekam Samarpayami............... '
            );
        case 'Sahasranamam':
            return (
                'Sahasranama Archana samarpanam.............. ' +
                'Om Namo Bhagavate Vasudevaya................. ' +
                'Sahasra Namam. Sahasra Pushpam............... ' +
                'Samarpayami................ '
            );
        case 'Ganapathi Homam':
            return (
                'Ganapathi Homam samarpanam................. ' +
                'Om Gam Ganapataye Namaha................... ' +
                'Swaha. Swaha. Poornahuti Samarpayami................. ' +
                'Vigna Nivaranam. Vigna Nashanam................... '
            );
        default:
            return (
                'Archana samarpanam................. ' +
                'Om Namo Narayanaya.................. '
            );
    }
}

// ── QUICK SCRIPT ─────────────────────────────────────────────
function buildQuickScript() {
    if (!currentBookingData) return 'Om Namah. Archana confirmed.';
    var b = currentBookingData;
    return (
        'Om Namah.............. ' +
        (b.devoteeName || '') + ' avargalukku............. ' +
        (b.gothram     || '') + ' gothram............. ' +
        (b.nakshatram  || '') + ' nakshatram.............. ' +
        (b.archanaType || '') + ' booking confirmed............... ' +
        'Sarva Mangala Prapthirastu.............. ' +
        'Govinda............. Govinda'
    );
}

// ════════════════════════════════════════════════════════════
// CORE SPEAKING ENGINE
// Splits text into chunks for more natural pausing
// ════════════════════════════════════════════════════════════
function speakPriestStyle(fullText, btnId) {

    if (!('speechSynthesis' in window)) {
        alert('Voice not supported. Please use Google Chrome browser.');
        return;
    }

    // Stop any existing speech
    window.speechSynthesis.cancel();
    isSpeaking = true;

    // Update button
    var btn = document.getElementById(btnId);
    if (btn) {
        btn.textContent = '🔊 Chanting Archana...';
        btn.disabled    = true;
        btn.style.opacity = '0.7';
    }

    // Show voice text display
    var display = document.getElementById('voiceDisplay');
    if (display) {
        display.style.display = 'block';
        display.innerHTML =
            '<div style="color:#FFD700; font-weight:600; margin-bottom:8px;">' +
            '🎵 Archana being chanted:</div>' +
            '<div style="font-style:italic; line-height:2; color:#ffe066;">' +
            fullText.replace(/\.\.\.\.\.\.\.\.\.\.\.\./g, '...') +
            '</div>';
    }

    // Wait for voices to load then speak
    function doSpeak() {
        var voices = window.speechSynthesis.getVoices();

        // Best voices for priest effect:
        // 1. en-IN (Indian English) — closest to South Indian accent
        // 2. en-GB Male — deeper, formal
        // 3. Any English voice
        var selectedVoice =
            voices.find(function(v) {
                return v.lang === 'en-IN';
            }) ||
            voices.find(function(v) {
                return v.lang === 'en-GB' &&
                       v.name.toLowerCase().indexOf('male') !== -1;
            }) ||
            voices.find(function(v) {
                return v.lang === 'en-GB';
            }) ||
            voices.find(function(v) {
                return v.name.toLowerCase().indexOf('david') !== -1;
            }) ||
            voices.find(function(v) {
                return v.name.toLowerCase().indexOf('mark') !== -1;
            }) ||
            voices.find(function(v) {
                return v.lang.startsWith('en');
            });

        var utterance = new SpeechSynthesisUtterance(fullText);

        // Priest style settings
        utterance.rate   = 0.65;  // Slow — like a priest chanting
        utterance.pitch  = 0.7;   // Deep — masculine priest voice
        utterance.volume = 1.0;

        if (selectedVoice) {
            utterance.voice = selectedVoice;
            console.log('Using voice:', selectedVoice.name, selectedVoice.lang);
        }

        utterance.onend = function() {
            isSpeaking = false;
            if (btn) {
                btn.textContent = '🔊 Play Archana Again';
                btn.disabled    = false;
                btn.style.opacity = '1';
            }
            if (display) {
                display.innerHTML +=
                    '<div style="color:#4ade80; margin-top:12px; ' +
                    'font-weight:600;">✅ Archana completed. ' +
                    'Sarva Mangala Prapthirastu. 🙏</div>';
            }
        };

        utterance.onerror = function(e) {
            console.error('Speech error:', e);
            isSpeaking = false;
            if (btn) {
                btn.textContent = '🔊 Retry';
                btn.disabled    = false;
                btn.style.opacity = '1';
            }
        };

        window.speechSynthesis.speak(utterance);

        // Chrome bug fix: keep speech alive for long texts
        var keepAlive = setInterval(function() {
            if (!isSpeaking) {
                clearInterval(keepAlive);
                return;
            }
            if (window.speechSynthesis.speaking) {
                window.speechSynthesis.pause();
                window.speechSynthesis.resume();
            } else {
                clearInterval(keepAlive);
            }
        }, 10000);
    }

    // Load voices
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