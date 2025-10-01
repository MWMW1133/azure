<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>로그인</title>
    <link rel="stylesheet" href="/css/login.css" />
  </head>
  <body>
    <form class="login-form" action="/login" method="post">
      <img class="logo" src="/images/azure2-1.png" alt="로고" />
      <h1 class="title">Login</h1>
      <p class="subtitle">경량화 스마트 플로우 PM 매니저</p>

      <input
        type="text"
        name="userEmail"
        class="input-field"
        placeholder="이메일 입력"
      />
      <input
        type="password"
        name="password"
        class="input-field"
        placeholder="PASSWORD 입력"
      />

      <button type="submit" class="btn btn-primary">로그인</button>
      <button
        type="button"
        class="btn btn-secondary"
        onclick="location.href='/signup'"
      >
        회원가입
      </button>
    </form>
  </body>
</html>
