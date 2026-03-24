// ============================================================
// BOOKING FORM - Price Display Logic
// ============================================================

const archanaPrices = {
    'Pushparchana'    : '₹10',
    'Kumkumarchana'   : '₹15',
    'Abhishekam'      : '₹40',
    'Sahasranamam'    : '₹30',
    'Ganapathi Homam' : '₹50'
};

// Update amount display when archana type is selected
document.querySelectorAll('input[name="archanaType"]').forEach(radio => {
    radio.addEventListener('change', function() {
        const selected = this.value;
        const price = archanaPrices[selected] || '₹0';
        document.getElementById('totalAmount').textContent = price;
        document.getElementById('amountDisplay').style.display = 'block';

        // Highlight selected option box
        document.querySelectorAll('.option-box').forEach(box => {
            box.classList.remove('selected');
        });
        this.nextElementSibling.classList.add('selected');
    });
});

// Phone number: only allow digits
document.getElementById('phone').addEventListener('input', function() {
    this.value = this.value.replace(/[^0-9]/g, '');
});
// ── UPDATE TOTAL AMOUNT ───────────────────────
function updateAmount(archanaType) {
    var price       = PRICES[archanaType] || 0;
    var display     = document.getElementById('amountDisplay');
    var totalEl     = document.getElementById('totalAmount');

    if (price > 0) {
        if (display)  display.style.display = 'block';
        if (totalEl)  totalEl.textContent   = '₹' + price;
        console.log('Selected: ' + archanaType + ' = ₹' + price);
    } else {
        if (display)  display.style.display = 'none';
        if (totalEl)  totalEl.textContent   = '₹0';
    }
}

// ── FORM VALIDATION ───────────────────────────
function setupFormValidation() {
    var form = document.getElementById('bookingForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        var name      = document.getElementById('name').value.trim();
        var gothram   = document.getElementById('gothram').value;
        var nakshatram= document.getElementById('nakshatram').value;
        var phone     = document.getElementById('phone').value.trim();
        var timeSlot  = document.getElementById('timeSlot').value;
        var archana   = document.querySelector('input[name="archanaType"]:checked');

        // Validate all fields
        if (!name) {
            alert('Please enter your full name.');
            e.preventDefault(); return;
        }
        if (!gothram) {
            alert('Please select your Gothram.');
            e.preventDefault(); return;
        }
        if (!nakshatram) {
            alert('Please select your Nakshatram.');
            e.preventDefault(); return;
        }
        if (!phone || phone.length !== 10) {
            alert('Please enter a valid 10-digit phone number.');
            e.preventDefault(); return;
        }
        if (!archana) {
            alert('Please select an Archana type.');
            e.preventDefault(); return;
        }
        if (!timeSlot) {
            alert('Please select a time slot.');
            e.preventDefault(); return;
        }

        // All valid — show loading
        var btn = form.querySelector('.btn-submit');
        if (btn) {
            btn.textContent = '⏳ Processing...';
            btn.disabled    = true;
        }

        console.log('Form submitted:', {
            name, gothram, nakshatram, phone,
            archana: archana.value,
            price: PRICES[archana.value],
            timeSlot
        });
    });
}