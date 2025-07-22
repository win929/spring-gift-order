document.addEventListener('DOMContentLoaded', function () {
  // --- 회원가입 및 로그인 처리 ---
  const registerForm = document.getElementById('register-form');
  if (registerForm) {
    registerForm.addEventListener('submit', handleAuthFormSubmit);
  }

  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', handleAuthFormSubmit);
  }

  // --- 이벤트 위임을 사용한 통합 이벤트 리스너 ---
  document.body.addEventListener('click', function (event) {
    const clickableRow = event.target.closest('.clickable-row');
    if (clickableRow) {
      const link = clickableRow.dataset.link;
      if (link) {
        window.location.href = link;
      }
      return;
    }

    if (event.target.classList.contains('add-to-wish-btn') || event.target.classList.contains('delete-from-wish-btn')) {
      handleWishAction(event);
    }
  });

  // --- 삭제 확인 ---
  document.querySelectorAll(".delete-form").forEach(form => {
    form.addEventListener("click", function (event) {
      event.stopPropagation();
    });

    form.addEventListener("submit", function (event) {
      if (!confirm("정말 삭제하시겠습니까?")) {
        event.preventDefault();
      }
    });
  });

  const addOptionForm = document.getElementById('add-option-form');
  if (addOptionForm) {
    addOptionForm.addEventListener('submit', function (e) {
      e.preventDefault();
      const productId = this.dataset.productId;
      addOption(productId, this);
    });
  }
});

// 로그인/회원가입 폼 제출 비동기 함수
async function handleAuthFormSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const url = form.action;
  const email = form.email.value;
  const password = form.password.value;

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({email, password})
    });

    if (response.ok) {
      // 성공 시 HttpOnly 쿠키가 자동으로 브라우저에 저장됩니다.
      const successMsg = (url.includes('register')) ? '회원가입 성공!' : '로그인 성공!';
      alert(successMsg);
      window.location.href = '/members/products';
    } else {
      // 실패 시, 서버로부터 받은 JSON 에러 메시지를 파싱하여 사용합니다.
      const errorData = await response.json();
      const errorMessage = errorData.message || '알 수 없는 오류가 발생했습니다.';
      alert(
          `${(url.includes('register')) ? '회원가입' : '로그인'} 실패: ${errorMessage}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('요청 중 오류가 발생했습니다. 서버 상태를 확인해주세요.');
  }
}

// 위시리스트 추가/삭제 비동기 함수
async function handleWishAction(event) {
  event.stopPropagation();
  const button = event.target;
  const productId = button.dataset.productId;
  const wishId = button.dataset.wishId;

  // 더 이상 localStorage에서 토큰을 가져오지 않습니다. 브라우저가 자동으로 쿠키를 전송합니다.
  const url = (productId) ? '/api/wishes' : `/api/wishes/${wishId}`;
  const method = (productId) ? 'POST' : 'DELETE';

  if (method === 'DELETE' && !confirm('정말 삭제하시겠습니까?')) {
    return;
  }

  try {
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization' 헤더는 쿠키 사용으로 인해 더 이상 필요 없습니다.
      },
      body: (productId) ? JSON.stringify({productId: productId}) : null
    });

    if (response.ok) {
      const successMsg = (method === 'POST') ? '위시리스트에 상품을 추가했습니다!'
          : '위시리스트에서 상품을 삭제했습니다.';
      alert(successMsg);
      if (method === 'DELETE') {
        window.location.reload(); // 삭제 후 페이지 새로고침
      }
    } else {
      const errorData = await response.json();
      const errorMessage = errorData.message || '요청에 실패했습니다.';
      alert(`요청 실패: ${errorMessage}`);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('요청 중 오류가 발생했습니다. 서버 상태를 확인해주세요.');
  }
}

async function addOption(productId, form) {
  const formData = new FormData(form);
  const data = Object.fromEntries(formData.entries());

  try {
    const response = await fetch(`/api/products/${productId}/options`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data)
    });

    if (response.ok) {
      alert('옵션이 추가되었습니다.');
      window.location.reload();
    } else {
      const error = await response.json();
      alert('오류: ' + (error.message || '알 수 없는 오류'));
    }
  } catch (error) {
    alert('요청 중 오류가 발생했습니다.');
  }
}

async function updateOption(productId, optionId) {
  const nameInput = document.getElementById(`name-${optionId}`);
  const quantityInput = document.getElementById(`quantity-${optionId}`);
  const data = {
    name: nameInput.value,
    quantity: parseInt(quantityInput.value, 10)
  };

  try {
    const response = await fetch(`/api/products/${productId}/options/${optionId}`, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data)
    });

    if (response.ok) {
      alert('옵션이 수정되었습니다.');
      window.location.reload();
    } else {
      const error = await response.json();
      alert('오류: ' + (error.message || '알 수 없는 오류'));
    }
  } catch (error) {
    alert('요청 중 오류가 발생했습니다.');
  }
}

async function deleteOption(productId, optionId) {
  if (!confirm('정말 삭제하시겠습니까? 상품에는 최소 1개의 옵션이 있어야 합니다.')) {
    return;
  }

  try {
    const response = await fetch(`/api/products/${productId}/options/${optionId}`, {
      method: 'DELETE'
    });

    if (response.ok) {
      alert('옵션이 삭제되었습니다.');
      window.location.reload(); // 성공 시 페이지 새로고침
    } else {
      const error = await response.json();
      alert('오류: ' + (error.message || '알 수 없는 오류가 발생했습니다.'));
    }
  } catch (error) {
    console.error('Error:', error);
    alert('요청 중 오류가 발생했습니다.');
  }
}