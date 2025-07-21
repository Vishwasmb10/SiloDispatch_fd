document.getElementById('loadOrdersBtn').addEventListener('click', async () => {
  const driverId = document.getElementById('driverIdInput').value;
  const container = document.getElementById('ordersContainer');
  if (!driverId) return alert("Enter Driver ID");

  try {
    const res = await fetch(`/api/driver/${driverId}/orders`);
    const batches = await res.json();
    container.innerHTML = '';

    if (batches.length === 0) {
      container.innerHTML = '<p>No assigned batches.</p>';
      return;
    }

    batches.forEach(batch => {
      const batchDiv = document.createElement('div');
      batchDiv.innerHTML = `<h3>Batch ${batch.batchId}</h3>`;

      batch.orders.forEach(order => {
        const isDelivered = order.deliveryStatus === 'DELIVERED';
        const isOtpVerified = order.otpStatus === 'VERIFIED';
        const isPrepaid = order.paymentType === 'PREPAID';
        const showPaymentBtn = isOtpVerified && !isPrepaid && !isDelivered;

        const orderDiv = document.createElement('div');
        orderDiv.className = 'order';
        orderDiv.innerHTML = `
          <p><strong>Order ID:</strong> ${order.orderId}</p>
          <p><strong>Customer:</strong> ${order.customerName}</p>
          <p><strong>Phone:</strong> +917892933981</p>
          <p><strong>Payment Type:</strong> ${order.paymentType}</p>
          <p><strong>Status:</strong> ${order.deliveryStatus}</p>
          <input placeholder="Enter OTP" id="otp-${order.orderId}"
            ${isDelivered || isOtpVerified ? 'disabled' : ''} />
          <br>
          <button id="sendBtn-${order.orderId}"
            ${isDelivered || isOtpVerified ? 'disabled' : ''}
            onclick="sendOtp(${order.orderId}, '+917892933981')">Send OTP</button>

          <button id="verifyBtn-${order.orderId}"
            ${isDelivered || isOtpVerified ? 'disabled' : ''}
            onclick="verifyOtp(${order.orderId}, '${order.paymentType}', '${order.paymentStatus}')">Verify OTP</button>

          <button id="payBtn-${order.orderId}"
            ${showPaymentBtn ? '' : 'disabled'}
            onclick="redirectToPayment(${order.orderId})">Continue to Payment</button>

          <p id="msg-${order.orderId}"></p>
        `;
        batchDiv.appendChild(orderDiv);
      });

      container.appendChild(batchDiv);
    });
  } catch (err) {
    container.innerHTML = `<p style="color:red;">Error loading orders: ${err.message}</p>`;
  }
});

async function sendOtp(orderId, phoneNumber) {
  const msg = document.getElementById(`msg-${orderId}`);
  msg.textContent = 'Sending OTP...';
  try {
    const res = await fetch('/api/otp/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId, phoneNumber }),
    });
    const text = await res.text();
    msg.textContent = text;
  } catch (err) {
    msg.textContent = 'Failed to send OTP';
  }
}

async function verifyOtp(orderId, paymentType, paymentStatus) {
  const otp = document.getElementById(`otp-${orderId}`).value;
  const msg = document.getElementById(`msg-${orderId}`);
  const payBtn = document.getElementById(`payBtn-${orderId}`);
  const sendBtn = document.getElementById(`sendBtn-${orderId}`);
  const verifyBtn = document.getElementById(`verifyBtn-${orderId}`);
  const otpInput = document.getElementById(`otp-${orderId}`);

  if (!otp) return (msg.textContent = 'Enter OTP');

  try {
    const res = await fetch('/api/otp/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId, otp }),
    });
    const text = await res.text();
    msg.textContent = text;

    if (res.ok) {
      // Disable input and buttons
      otpInput.disabled = true;
      sendBtn.disabled = true;
      verifyBtn.disabled = true;

      if (paymentType === 'PREPAID') {
        msg.textContent += ' | Delivery marked successful (PREPAID).';
      } else {
        payBtn.disabled = false;
        msg.textContent += ' | OTP verified. Continue to payment.';
      }
    }
  } catch (err) {
    msg.textContent = 'Verification failed';
  }
}

function redirectToPayment(orderId) {
  console.log("Redirecting to Payment.html...");
  localStorage.setItem('currentOrderId', orderId);
  window.location.href = '/Payment.html';
}
