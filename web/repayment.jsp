<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Trả nợ - P2P Lending</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
</head>
<body class="dashboard-wrapper">
    <div class="sidebar">
        <div class="sidebar-brand">🏛️ <span>P2P BORROWER</span></div>
        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/BorrowerDashboardServlet">📊 Tổng quan</a></li>
            <li class="active"><a href="${pageContext.request.contextPath}/RepaymentServlet">💳 Trả nợ theo kỳ</a></li>
        </ul>
        <form action="${pageContext.request.contextPath}/logout" method="POST"><button type="submit" class="btn-logout">Đăng xuất</button></form>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">Quản lý trả nợ</div>
            <span>Xin chào, <strong>${borrowerName}</strong></span>
        </div>
        <div class="container">
            <c:if test="${hasOverdue}">
                <div class="alert-banner alert-banner-danger">
                    <strong>⚠️ Nợ quá hạn:</strong> Bạn có khoản vay quá hạn. Chức năng <b>Đăng ký vay mới</b> đã bị khóa.
                </div>
            </c:if>
            <c:if test="${param.msg == 'ok'}"><div class="alert-banner alert-banner-success">Thanh toán thành công.</div></c:if>
            <c:if test="${param.msg == 'insufficient'}"><div class="alert-banner alert-banner-danger">Số dư ví không đủ.</div></c:if>

            <div class="data-card">
                <h4>Danh sách khoản vay đang hoạt động</h4>
                <table class="table-loan">
                    <thead>
                        <tr><th>Mã</th><th>Số tiền</th><th>Kỳ</th><th>Hạn</th><th>Phải trả</th><th>Trạng thái</th><th>Thao tác</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="view" items="${repaymentViews}">
                        <c:set var="loan" value="${view.loan}"/>
                        <tr>
                            <td>#${loan.loanId}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td>${loan.paidPeriods + 1} / ${loan.termMonths}</td>
                            <td><fmt:formatDate value="${loan.dueDate}" pattern="dd/MM/yyyy"/></td>
                            <td><fmt:formatNumber value="${view.totalDue}" groupingUsed="true"/> đ
                                <c:if test="${view.penalty > 0}"><br/><small>Phạt: <fmt:formatNumber value="${view.penalty}" groupingUsed="true"/> đ</small></c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${view.displayStatus == 'overdue'}"><span class="status-overdue">Quá hạn</span></c:when>
                                    <c:when test="${view.displayStatus == 'completed'}"><span class="status-completed">Completed</span></c:when>
                                    <c:otherwise><span class="status-processing">Processing</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${view.displayStatus != 'completed'}">
                                    <form action="${pageContext.request.contextPath}/RepaymentServlet" method="POST">
                                        <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                        <button type="submit" class="btn-submit" style="width:auto;padding:8px 14px;">Thanh toán kỳ</button>
                                    </form>
                                </c:if>
                                <a href="${pageContext.request.contextPath}/ContractServlet?loanId=${loan.loanId}" target="_blank">Hợp đồng</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty repaymentViews}">
                        <tr><td colspan="7" style="text-align:center;color:#94a3b8;">Không có khoản vay cần trả.</td></tr>
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
                <h5>${n.title}</h5><p>${n.message}</p>
            </div>
        </c:forEach>
    </aside>

    <footer class="site-footer">Email: admin@gmail.com - Số điện thoại: 01234567891</footer>
    <button id="backToTop">↑</button>
    <script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>
</body>
</html>
