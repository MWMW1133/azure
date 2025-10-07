<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>회원가입</title>
    <link rel="stylesheet" href="/css/signup.css" />
</head>
<body>
    <form action="/signup" method="post" class="signup-form" id="signup-form">
        <img src="/images/azure2-1.png" alt="Azure Logo" class="logo" />
        <h1 class="title">Sign Up</h1>
        <p class="subtitle">경량화 스마트 플로우 PM 매니저</p>

        <div class="input-group">
            <div class="input-with-button">
                <input type="text" name="userId" id="userId" class="input-field" placeholder="아이디" />
                <button type="button" class="btn-check" id="btn-check-id">중복 확인</button>
            </div>
            <p id="feedback-userId" class="feedback-text"></p>
        </div>

        <div class="input-group">
            <input type="password" name="userPwd" id="userPwd" class="input-field" placeholder="PASSWORD 입력" />
            <p class="feedback-text" id="feedback-userPwd"></p>
        </div>

        <div class="input-group">
            <input type="password" name="userPwdChk" id="userPwdChk" class="input-field" placeholder="PASSWORD 확인" />
            <p id="feedback-userPwdChk" class="feedback-text"></p>
        </div>

        <div class="input-group">
            <input type="text" name="userName" id="userName" class="input-field" placeholder="성명" />
            <p class="feedback-text" id="feedback-userName"></p>
        </div>

        <div class="input-group" id="company-input-group" style="display: none">
            <input type="text" name="companyName" id="companyName" class="input-field" placeholder="회사 명" />
            <p class="feedback-text" id="feedback-companyName"></p>
        </div>

        <p id="admin-signup-link" class="admin-link">관리자로 가입하기</p>
        <button type="submit" class="btn-submit">회원가입</button>
    </form>
</body>

<script>
    const signupForm = document.getElementById("signup-form");
    const adminLink = document.getElementById("admin-signup-link");
    const companyInputGroup = document.getElementById("company-input-group");
    const btnCheckId = document.getElementById("btn-check-id");

    const userIdInput = document.getElementById("userId");
    const userPwdInput = document.getElementById("userPwd");
    const userPwdChkInput = document.getElementById("userPwdChk");
    const userNameInput = document.getElementById("userName");
    const companyNameInput = document.getElementById("companyName");

    let isIdChecked = false;

    // 관리자 가입 창 열닫
    adminLink.addEventListener("click", function () {
        const isHidden = companyInputGroup.style.display === "none";
        companyInputGroup.style.display = isHidden ? "flex" : "none";
        adminLink.innerText = isHidden ? "닫기" : "관리자로 가입하기";
    });

    // 아이디 중복 확인
    btnCheckId.addEventListener("click", function () {
        if (userIdInput.value.trim() === "") {
            showFeedback(userIdInput, "아이디를 입력해주세요.", "red");
            isIdChecked = false;
            return;
        }
        ///////////////////////////아이디 중복 검사///////////////////////////////
        showFeedback(userIdInput, "사용 가능한 아이디입니다.", "green");
        isIdChecked = true;
    });
    
    // 아이디 바꾸면 중복 다시 검사하게
    userIdInput.addEventListener("input", () => {
        isIdChecked = false;
        hideFeedback(userIdInput);
    });

    // 비밀번호 확인 검사
    userPwdChkInput.addEventListener("input", function () {
        if (userPwdChkInput.value !== userPwdInput.value) {
            showFeedback(userPwdChkInput, "비밀번호가 일치하지 않습니다.", "red");
        } else {
            showFeedback(userPwdChkInput, "비밀번호가 일치합니다.", "green");
        }
    });

    // 회원가입 폼 제출
    signupForm.addEventListener("submit", function (event) {
        event.preventDefault();
        hideAllFeedbacks();
        
        let isValid = true;
        let firstInvalidInput = null;

        // 관리자 여부 확인
        const isAdminSignup = companyInputGroup.style.display !== "none";

        // 관리자일 때만 회사명 입력 확인
        if (isAdminSignup && companyNameInput.value.trim() === "") {
            isValid = false;
            showFeedback(companyNameInput, "회사명을 입력해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = companyNameInput;
        }
        if (userNameInput.value.trim() === "") {
            isValid = false;
            showFeedback(userNameInput, "성명을 입력해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = userNameInput;
        }
        if (userPwdChkInput.value.trim() === "") {
            isValid = false;
            showFeedback(userPwdChkInput, "비밀번호 확인을 입력해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = userPwdChkInput;
        }
        if (userPwdInput.value.trim() === "") {
            isValid = false;
            showFeedback(userPwdInput, "비밀번호를 입력해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = userPwdInput;
        }
        if (userIdInput.value.trim() === "") {
            isValid = false;
            showFeedback(userIdInput, "아이디를 입력해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = userIdInput;
        }
        
        // 아이디 중복 확인 여부 검사
        if (!isIdChecked) {
            isValid = false;
            showFeedback(userIdInput, "아이디 중복 확인을 해주세요.", "red");
            if (!firstInvalidInput) firstInvalidInput = userIdInput;
        }

        // 비밀번호 일치 검사
        if (userPwdInput.value !== userPwdChkInput.value) {
            isValid = false;
            showFeedback(userPwdChkInput, "비밀번호가 일치하지 않습니다.", "red");
            if (!firstInvalidInput) firstInvalidInput = userPwdChkInput;
        }
        
        // 다 괜찮으면 폼 제출, 아니면 오류난 곳으로 이동
        if (isValid) {
            signupForm.submit();
        } else {
            if (firstInvalidInput) {
                firstInvalidInput.focus();
            }
        }
    });

    // 텍스트 띄우기
    function showFeedback(element, message, color) {
        const feedbackElement = document.getElementById("feedback-" + element.id);
        feedbackElement.textContent = message;
        feedbackElement.style.color = (color === "green") ? "#28a745" : "#d42b2b";
    }

    //텍스트 지우기
    function hideFeedback(element) {
        const feedbackElement = document.getElementById("feedback-" + element.id);
        feedbackElement.textContent = "";
    }

    function hideAllFeedbacks() {
        const feedbackElements = document.querySelectorAll('.feedback-text');
        feedbackElements.forEach(el => el.textContent = '');
    }
</script>
</html>