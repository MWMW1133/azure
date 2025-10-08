
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Project API Test</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<h1>Project API Test</h1>

<!-- 프로젝트 생성 -->
<h2>Create Project</h2>
<form id="createForm">
    Organization ID: <input type="text" name="organizationId" value="1"/><br/>
    Owner ID: <input type="text" name="ownerId" value="10"/><br/>
    Name: <input type="text" name="name" value="테스트프로젝트"/><br/>
    Description: <input type="text" name="description" value="설명"/><br/>
    <button type="submit">Create</button>
</form>
<pre id="createResult"></pre>

<!-- 프로젝트 단건 조회 -->
<h2>Get Project</h2>
<form id="getForm">
    Project ID: <input type="text" name="id" value="1"/><br/>
    <button type="submit">Get</button>
</form>
<pre id="getResult"></pre>

<!-- 사용자별 프로젝트 목록 -->
<h2>List Projects By User</h2>
<form id="listForm">
    User ID: <input type="text" name="userId" value="10"/><br/>
    <button type="submit">List</button>
</form>
<pre id="listResult"></pre>

<!-- 프로젝트 멤버 추가 -->
<h2>Add Member</h2>
<form id="addMemberForm">
    Project ID: <input type="text" name="projectId" value="1"/><br/>
    User ID: <input type="text" name="userId" value="12"/><br/>
    Role: <input type="text" name="role" value="MEMBER"/><br/>
    <button type="submit">Add</button>
</form>
<pre id="addMemberResult"></pre>

<!-- 프로젝트 멤버 제거 -->
<h2>Remove Member</h2>
<form id="removeMemberForm">
    Project ID: <input type="text" name="projectId" value="1"/><br/>
    User ID: <input type="text" name="userId" value="12"/><br/>
    <button type="submit">Remove</button>
</form>
<pre id="removeMemberResult"></pre>

<script>
    // 프로젝트 생성
    $("#createForm").submit(function (e) {
        e.preventDefault();
        $.post("/api/projects", $(this).serialize(), function (data) {
            $("#createResult").text(JSON.stringify(data, null, 2));
        });
    });

    // 프로젝트 단건 조회
    $("#getForm").submit(function (e) {
        e.preventDefault();
        const id = $(this).find("input[name='id']").val();
        $.get("/api/projects/" + id, function (data) {
            $("#getResult").text(JSON.stringify(data, null, 2));
        });
    });

    // 사용자별 프로젝트 목록
    $("#listForm").submit(function (e) {
        e.preventDefault();
        const userId = $(this).find("input[name='userId']").val();
        $.get("/api/projects?userId=" + userId, function (data) {
            $("#listResult").text(JSON.stringify(data, null, 2));
        });
    });

    // 멤버 추가
    $("#addMemberForm").submit(function (e) {
        e.preventDefault();
        const projectId = $(this).find("input[name='projectId']").val();
        $.post("/api/projects/" + projectId + "/members", $(this).serialize(), function (data) {
            $("#addMemberResult").text(JSON.stringify(data, null, 2));
        });
    });

    // 멤버 제거
    $("#removeMemberForm").submit(function (e) {
        e.preventDefault();
        const projectId = $(this).find("input[name='projectId']").val();
        const userId = $(this).find("input[name='userId']").val();
        $.ajax({
            url: "/api/projects/" + projectId + "/members/" + userId,
            type: "DELETE",
            success: function () {
                $("#removeMemberResult").text("Member removed successfully");
            }
        });
    });
</script>
</body>
</html>
