<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết gói vốn - P2P Lending</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
</head>
<body class="dashboard-wrapper">
    <div class="sidebar">
        <div>
            <div class="sidebar-brand">🏛️ <span>P2P INVESTOR</span></div>
            <ul class="sidebar-menu">
                <li><a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=dashboard">Tổng Quan Main</a></li>
                <li class="active"><a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=market">Gọi Vốn Vay</a></li>
                <li><a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=profile">Hồ Sơ Đăng Ký</a></li>
                <li><a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=my_investments">Lịch Sử Góp Vốn</a></li>
            </ul>
        </div>
        <form action="${pageContext.request.contextPath}/logout" method="POST" style="margin:0;">
            <button type="submit" class="btn-logout">Đăng xuất</button>
        </form>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">Chi tiết gói gọi vốn #${loanDetail.loanId}</div>
            <div class="user-info" style="text-align: right;">
                <div>Xin chào, <strong>${investorName}</strong>
                <c:if test="${not empty investor}">
                    | Ví: <fmt:formatNumber value="${investor.walletBalance}" groupingUsed="true"/> đ
                </c:if></div>
            </div>
        </div>
        <div class="container">
            <div class="data-card" style="margin-bottom: 16px;">
                <h4>Thông tin gói vốn</h4>
                <table class="table-loan">
                    <tbody>
                        <tr><th style="width:200px;">Mã gói</th><td>#${loanDetail.loanId}</td></tr>
                        <tr><th>Người vay (ẩn danh)</th><td>${loanDetail.maskedBorrowerName}</td></tr>
                        <tr><th>Lãi suất</th><td>${loanDetail.interestRate}% / năm</td></tr>
                        <tr><th>Số tiền gọi vốn</th><td><fmt:formatNumber value="${loanDetail.amountRequested}" groupingUsed="true"/> đ</td></tr>
                        <tr><th>Kỳ hạn</th><td>${loanDetail.termMonths} tháng</td></tr>
                        <tr><th>Tiến độ</th><td><fmt:formatNumber value="${loanDetail.fundingProgress}" maxFractionDigits="1"/>%</td></tr>
                        <c:if test="${not empty loanDetail.cicIssuedDate}">
                            <tr><th>Ngày phát hành tài liệu</th><td><fmt:formatDate value="${loanDetail.cicIssuedDate}" pattern="dd/MM/yyyy"/></td></tr>
                        </c:if>
                    </tbody>
                </table>
                <p style="margin-top: 12px;">
                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=market" style="color:#2563eb;">← Quay lại Gọi Vốn Vay</a>
                </p>
            </div>

            <div class="data-card">
                <h4>Hồ sơ PDF đính kèm (gọi vốn vay)</h4>
                <c:choose>
                    <c:when test="${not empty loanDetail.cicPdfUrl}">
                        <c:set var="pdfHref" value="${fn:startsWith(loanDetail.cicPdfUrl, 'http://') or fn:startsWith(loanDetail.cicPdfUrl, 'https://') ? loanDetail.cicPdfUrl : pageContext.request.contextPath.concat('/uploads/').concat(loanDetail.cicPdfUrl)}"/>
                        <p style="margin-bottom: 12px;">
                            <a href="${pdfHref}" target="_blank" rel="noopener" style="color:#2563eb; text-decoration:underline; font-weight:bold;">📄 Mở PDF trong tab mới</a>
                        </p>
                        <iframe src="${pdfHref}" title="Hồ sơ PDF gói vốn" style="width:100%;min-height:520px;border:1px solid #e2e8f0;border-radius:8px;"></iframe>
                    </c:when>
                    <c:otherwise>
                        <p style="color:#94a3b8;">Chưa có file PDF đính kèm cho gói vốn này.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <aside class="notification-panel">
        <h4>🔔 Thông báo</h4>
        <c:forEach var="n" items="${notifications}">
            <div class="notif-item ${n.read ? '' : 'unread'}">
                <h5>${n.title}</h5>
                <p>${n.message}</p>
                <small><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </div>
        </c:forEach>
        <c:if test="${empty notifications}"><p style="color:#94a3b8;font-size:13px;">Chưa có thông báo.</p></c:if>
    </aside>

    <button id="backToTop">↑</button>
    <script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>
</body>
</html>
