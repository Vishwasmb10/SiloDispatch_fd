// ========== 1. Upload CSV ==========
document.getElementById('uploadForm').addEventListener('submit', async function (e) {
  e.preventDefault();
  const file = document.getElementById('csvFile').files[0];
  const msg = document.getElementById('uploadResponse');
  msg.className = 'response';

  if (!file) return (msg.textContent = 'Please select a file.', msg.classList.add('error'));

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res = await fetch('/api/orders/upload', { method: 'POST', body: formData });
    const text = await res.text();
    msg.textContent = text;
    msg.classList.add(res.ok ? 'success' : 'error');
  } catch (err) {
    msg.textContent = 'Upload failed: ' + err.message;
    msg.classList.add('error');
  }
});

// ========== 2. Generate Batches ==========
document.getElementById('generateBtn').addEventListener('click', async () => {
  const msg = document.getElementById('batchResponse');
  msg.className = 'response';
  msg.textContent = 'Generating...';

  try {
    const res = await fetch('/api/batching/generate', { method: 'POST' });
    const text = await res.text();
    msg.textContent = text;
    msg.classList.add(res.ok ? 'success' : 'error');
  } catch (err) {
    msg.textContent = 'Batch generation failed: ' + err.message;
    msg.classList.add('error');
  }
});

// ========== 3. Load Available Drivers ==========
async function loadDrivers() {
  const driverList = document.getElementById('driverList');
  driverList.innerHTML = 'Loading...';

  try {
    const res = await fetch('/api/driver/available');
    const drivers = await res.json();

    driverList.innerHTML = '';
    drivers.forEach(driver => {
      const label = document.createElement('label');
      label.innerHTML = `<input type="checkbox" value="${driver.id}"> ${driver.name} (ID: ${driver.id})`;
      driverList.appendChild(label);
    });
  } catch (err) {
    driverList.innerHTML = '<p style="color: red;">Failed to load drivers.</p>';
  }
}
document.addEventListener('DOMContentLoaded', loadDrivers);

// ========== 4. Assign Batches ==========
document.getElementById('driverForm').addEventListener('submit', async function (e) {
  e.preventDefault();
  const checked = document.querySelectorAll('#driverList input:checked');
  const msg = document.getElementById('assignResponse');
  msg.className = 'response';

  if (checked.length === 0) {
    msg.textContent = 'Select at least one driver.';
    msg.classList.add('error');
    return;
  }

  const ids = Array.from(checked).map(cb => parseInt(cb.value));

  try {
    const res = await fetch('/api/assign/batches', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ids),
    });
    const text = await res.text();
    msg.textContent = text;
    msg.classList.add(res.ok ? 'success' : 'error');
  } catch (err) {
    msg.textContent = 'Assignment failed: ' + err.message;
    msg.classList.add('error');
  }
});

// ========== 5. Load Cash Info ==========
async function loadCashInfo() {
  const list = document.getElementById('cashList');
  const select = document.getElementById('driverSelect');
  list.innerHTML = 'Loading...';
  select.innerHTML = '';

  try {
    const res = await fetch('/api/drivers/cash-in-hand');
    const cashInfo = await res.json();

    if (cashInfo.length === 0) {
      list.innerHTML = '<p>No driver cash info found.</p>';
      return;
    }

    list.innerHTML = '<ul>' +
      cashInfo.map(d =>
        `<li><strong>${d.driverName}</strong> (ID: ${d.driverId}) - ₹${d.cashInHand}</li>`
      ).join('') + '</ul>';

    select.innerHTML = '<option value="">-- Select Driver --</option>' +
      cashInfo.map(d =>
        `<option value="${d.driverId}">${d.driverName} (ID: ${d.driverId})</option>`
      ).join('');
  } catch (err) {
    list.innerHTML = '<p style="color: red;">Failed to load cash info.</p>';
  }
}
document.addEventListener('DOMContentLoaded', loadCashInfo);

// ========== 6. Settle Cash ==========
document.getElementById('settleForm').addEventListener('submit', async function (e) {
  e.preventDefault();

  const driverId = document.getElementById('driverSelect').value;
  const amount = document.getElementById('amountInput').value;
  const msg = document.getElementById('settleResponse');
  msg.className = 'response';

  if (!driverId || !amount) {
    msg.textContent = 'Please select driver and enter amount.';
    msg.classList.add('error');
    return;
  }

  try {
    const res = await fetch('/api/drivers/settle-cash', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ driverId: parseInt(driverId), collectedAmount: parseFloat(amount) }),
    });

    const text = await res.text();
    msg.textContent = text;
    msg.classList.add(res.ok ? 'success' : 'error');
    await loadCashInfo(); // refresh cash info after settlement
  } catch (err) {
    msg.textContent = 'Settlement failed: ' + err.message;
    msg.classList.add('error');
  }
});
