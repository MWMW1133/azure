<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>문서 작성</title>
    <script src="https://cdn.ckeditor.com/4.22.1/full/ckeditor.js"></script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/files.css">

    <style>
        body {
            margin: 40px;
            font-family: 'Malgun Gothic', sans-serif;
        }
        #editorContainer {
            border: 1px solid #ccc;
            padding: 10px;
        }
        button {
            margin-top: 20px;
        }
    </style>
</head>
<body>
<h2>회의록 작성</h2>
<form id="docForm"
      action="/projects/${projectId}/documents/save"
      method="post"
      class="mx-auto">

    <div class="mb-3">
        <label for="title" class="form-label fw-semibold">문서 제목</label>
        <input type="text"
               name="title"
               id="title"
               class="form-control form-control-lg shadow-sm"
               placeholder="회의록 제목을 입력하세요">
    </div>

    <div id="editorContainer" class="shadow-sm border rounded">
        <textarea name="content" id="editor">${templateHtml}</textarea>
    </div>

    <div class="text-end mt-4 d-flex justify-content-end gap-3">
        <!-- PDF로 저장 -->
        <button type="submit" class="btn btn-primary px-4" formaction="/projects/${projectId}/documents/save/pdf">
            <i class="bi bi-file-earmark-pdf"></i> PDF로 저장
        </button>

        <!-- Word로 저장 -->
<%--        <button type="submit" class="btn btn-outline-primary px-4" formaction="/projects/${projectId}/documents/save/docx">--%>
<%--            <i class="bi bi-file-earmark-word"></i> Word로 저장--%>
<%--        </button>--%>
    </div>
</form>
<script>
    CKEDITOR.replace('editor', {
        height: 800,
        allowedContent: true
    });
</script>
</body>
</html>
