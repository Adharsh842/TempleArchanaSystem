// ============================================================
// QR CODE SCANNER — Connects verification with voice archana
// ============================================================

var codeReader = null;

/**
 * Start camera QR scanner
 */
async function startScanner() {
    try {
        if (typeof ZXing === 'undefined') {
            alert('Scanner library not loaded. Use manual entry below.');
            return;
        }

        codeReader = new ZXing.BrowserQRCodeReader();

        document.getElementById('startBtn').style.display = 'none';
        document.getElementById('stopBtn').style.display  = 'inline-block';

        var videoElement = document.getElementById('video');

        await codeReader.decodeFromVideoDevice(
            null,
            videoElement,
            function(result, error) {
                if (result) {
                    var scannedText = result.getText();
                    console.log('Scanned:', scannedText);

                    // Extract booking ID from QR content
                    var bookingId = extractBookingId(scannedText);
                    stopScanner();
                    verifyBooking(bookingId);
                }
            }
        );

    } catch (err) {
        console.error('Camera error:', err);
        document.getElementById('startBtn').style.display = 'inline-block';
        document.getElementById('stopBtn').style.display  = 'none';
        alert('Camera not available. Please use manual entry below.');
    }
}

/**
 * Extract booking ID from QR content
 * QR content format: "TEMPLE ARCHANA BOOKING\nBookingID: TEMPLE-xxx\n..."
 */
function extractBookingId(scannedText) {
    // Try to find BookingID line
    var lines = scannedText.split('\n');
    for (var i = 0; i < lines.length; i++) {
        if (lines[i].startsWith('BookingID:')) {
            return lines[i].replace('BookingID:', '').trim();
        }
    }
    // If it starts with TEMPLE- directly
    if (scannedText.trim().startsWith('TEMPLE-')) {
        return scannedText.trim();
    }
    // Return as-is (manual entry)
    return scannedText.trim();
}

/**
 * Stop camera scanner
 */
function stopScanner() {
    if (codeReader) {
        try { codeReader.reset(); } catch(e) {}
    }
    var startBtn = document.getElementById('startBtn');
    var stopBtn  = document.getElementById('stopBtn');
    if (startBtn) startBtn.style.display = 'inline-block';
    if (stopBtn)  stopBtn.style.display  = 'none';
}

/**
 * Verify from manual input
 */
function verifyManual() {
    var input = document.getElementById('manualBookingId');
    if (!input || !input.value.trim()) {
        alert('Please enter a Booking ID first.');
        return;
    }
    verifyBooking(input.value.trim());
}

/**
 * Main verification function — calls backend API
 */
async function verifyBooking(bookingId) {
    if (!bookingId) {
        alert('Booking ID is empty.');
        return;
    }

    showState('loading');

    try {
        var response = await fetch('/api/verify/' + encodeURIComponent(bookingId));

        if (!response.ok) {
            throw new Error('Server returned: ' + response.status);
        }

        var data = await response.json();

        // Store data globally for voice functions
        currentBookingData   = data.booking   || null;
        currentArchanaScript = data.archanaScript || null;
        currentQuickScript   = data.quickScript   || null;

        if (data.success) {
            showSuccessResult(data.message, data.booking);
            // Auto-play quick announcement after 1 second
            setTimeout(function() {
                if (currentQuickScript) {
                    speakText(
                        currentQuickScript,
                        'quickAnnounceBtn',
                        'quick'
                    );
                }
            }, 800);

        } else if (data.alreadyVerified) {
            showAlreadyVerifiedResult(data.message, data.booking);
        } else {
            showErrorResult(data.message || 'Booking not found.');
        }

    } catch (err) {
        console.error('Verification error:', err);
        showErrorResult('Network error: ' + err.message +
                        '. Check if server is running.');
    }
}

/**
 * Show verified booking details
 */
function showSuccessResult(message, booking) {
    showState('success');

    var msgEl = document.getElementById('successMessage');
    if (msgEl) msgEl.textContent = message || 'Booking Verified!';

    var detailsEl = document.getElementById('devoteeDetails');
    if (detailsEl && booking) {
        detailsEl.innerHTML =
            '<table class="verify-table">' +
            '<tr><td>Booking ID</td>' +
                '<td><strong>' + booking.bookingId + '</strong></td></tr>' +
            '<tr><td>Devotee</td>' +
                '<td>' + booking.devoteeName + '</td></tr>' +
            '<tr><td>Gothram</td>' +
                '<td>' + booking.gothram + '</td></tr>' +
            '<tr><td>Nakshatram</td>' +
                '<td>' + booking.nakshatram + '</td></tr>' +
            '<tr><td>Archana</td>' +
                '<td><strong>' + booking.archanaType + '</strong></td></tr>' +
            '<tr><td>Time Slot</td>' +
                '<td>' + booking.timeSlot + '</td></tr>' +
            '<tr><td>Amount</td>' +
                '<td>&#8377;' + booking.amount + '</td></tr>' +
            '<tr><td>Status</td>' +
                '<td style="color:green"><strong>VERIFIED</strong></td></tr>' +
            '</table>';
    }
}

/**
 * Show already verified result
 */
function showAlreadyVerifiedResult(message, booking) {
    showState('success');
    var msgEl = document.getElementById('successMessage');
    if (msgEl) {
        msgEl.textContent = 'Already Verified';
        msgEl.style.color = '#d97706';
    }
    if (booking) showSuccessResult(message, booking);
}

/**
 * Show error result
 */
function showErrorResult(message) {
    showState('error');
    var errEl = document.getElementById('errorMessage');
    if (errEl) errEl.textContent = message || 'Booking not found.';
}

/**
 * Toggle which panel is visible
 */
function showState(state) {
    var states = ['default', 'loading', 'success', 'error'];
    states.forEach(function(s) {
        var el = document.getElementById(s + 'State');
        if (el) el.style.display = 'none';
    });
    var target = document.getElementById(state + 'State');
    if (target) target.style.display = 'block';
}
// scanner.js - Temple Archana System
// All scanner code is in scanner.html
console.log('Temple scanner ready.');

// Function to call ElevenLabs and play the custom Iyer voice
async function playIyerArchana(devoteeName, gothram, nakshatram) {
    
    // Your ElevenLabs API Key
    const ELEVENLABS_API_KEY = "sk_d52dfaede0f580fd9a62fe1b0a3bb516da8c9d5bb833289f"; 
    
    // Your custom Iyer Voice ID
    const VOICE_ID = "uqUqAfa0V2x6ZIcZ8jQa";

    // The phonetic script using the devotee's specific details
    const mantraText = `Om... Shree Vengata-ramanaaya namaha. Hari Om... Asya yajamaanasya... ${gothram} go-throth pannasya... ${nakshatram} nak-shat-rey, jaathasya... Shree ${devoteeName} naa-ma de-yasya... kshema, sthairya, dhiirya, aayu-raa-rogya... aishwaryaa-bhi-vrudhya-artham... ashtothara archanaam... karrish-yay.`;

    try {
        console.log(`Generating Archana Voice for ${devoteeName}...`);
        
        const response = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${VOICE_ID}`, {
            method: 'POST',
            headers: {
                'Accept': 'audio/mpeg',
                'xi-api-key': ELEVENLABS_API_KEY,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                text: mantraText,
                model_id: "eleven_multilingual_v2", // Must be v2 to handle the phonetic Sanskrit/Tamil accurately
                voice_settings: {
                    stability: 0.45,
                    similarity_boost: 0.85
                }
            })
        });

        if (response.ok) {
            const blob = await response.blob();
            const audioUrl = URL.createObjectURL(blob);
            const audio = new Audio(audioUrl);
            
            // Play the generated chanting
            audio.play();
            console.log("Archana playing successfully!");
        } else {
            const errorText = await response.text();
            console.error("ElevenLabs Error:", errorText);
            alert("Error playing voice. Check the console for details.");
        }
    } catch (error) {
        console.error("Failed to fetch audio:", error);
    }
}
// Original code (example)
document.getElementById("fishAudioBtn").addEventListener("click", async () => {
    try {
        const response = await fetch(`/api/fish-audio?lang=tamil`);
        const data = await response.json();
        playAudio(data.audioUrl);
    } catch (err) {
        console.error("Fish Audio error:", err);
    }
});

// Temporary bypass
document.getElementById("fishAudioBtn").disabled = true;
document.getElementById("fishAudioBtn").title = "Fish Audio disabled - API missing";