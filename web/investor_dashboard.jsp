<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Investor Dashboard - P2P Lending</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
</head>
<body class="dashboard-wrapper">
    <div class="sidebar">
        <div class="sidebar-brand">🏛️ <span>P2P INVESTOR</span></div>
        <ul class="sidebar-menu">
            <li class="active"><a href="${pageContext.request.contextPath}/InvestorDashboardServlet">📈 Sàn gọi vốn</a></li>
        </ul>
        <form action="${pageContext.request.contextPath}/logout" method="POST"><button type="submit" class="btn-logout">Đăng xuất</button></form>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">Khoản vay đang gọi vốn (funding)</div>
            <div class="user-info">
                Xin chào, <strong>${investorName}</strong>
                <c:if test="${not empty investor}">
                    | Ví: <fmt:formatNumber value="${investor.walletBalance}" groupingUsed="true"/> đ
                    | Đóng băng: <fmt:formatNumber value="${investor.frozenBalance}" groupingUsed="true"/> đ
                </c:if>
            </div>
        </div>
        <div class="container">
            <c:if test="${param.msg == 'pending'}"><div class="alert-banner alert-banner-success">Đã gửi yêu cầu góp vốn, chờ Admin duyệt.</div></c:if>
            <c:if test="${param.msg == 'insufficient'}"><div class="alert-banner alert-banner-danger">Số dư ví không đủ.</div></c:if>

            <div class="data-card">
                <h4>🌐 Gói vay trên sàn</h4>
                <table class="table-loan">
                    <thead>
                        <tr><th>Mã</th><th>Người vay</th><th>Lãi suất</th><th>Số tiền</th><th>Tiến độ</th><th>Góp vốn</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="loan" items="${fundingLoans}">
                        <c:set var="pct" value="${loan.fundingProgress}"/>
                        <tr>
                            <td>#${loan.loanId > 0 ? loan.loanId : loan.applicationId}</td>
                            <td>${loan.maskedBorrowerName}</td>
                            <td>${loan.interestRate}%</td>
                            <td><fmt:formatNumber value="${loan.amountRequested}" groupingUsed="true"/> đ</td>
                            <td>
                                <div style="width:80px;background:#e2e8f0;border-radius:10px;height:6px;display:inline-block;">
                                    <div style="width:${pct}%;background:#22c55e;height:100%;border-radius:10px;"></div>
                                </div> <fmt:formatNumber value="${pct}" maxFractionDigits="1"/>%
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/InvestmentController" method="POST" style="display:flex;gap:4px;flex-wrap:wrap;">
                                    <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                    <input type="number" name="amount" min="100000" step="100000" placeholder="Số tiền" required style="width:120px;padding:6px;"/>
                                    <input type="text" name="bankName" placeholder="Ngân hàng" required style="width:100px;padding:6px;"/>
                                    <input type="text" name="accountNumber" placeholder="STK" required style="width:100px;padding:6px;"/>
                                    <button type="submit" class="btn-submit" style="width:auto;padding:6px 12px;">Góp vốn</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty fundingLoans}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có gói đang gọi vốn.</td></tr>
                    </c:if>
                    </tbody>
                </table>
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

    <footer class="site-footer">Email: admin@gmail.com - Số điện thoại: 01234567891</footer>
    <button id="backToTop">↑</button>
    <script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>
</body>
</html>
