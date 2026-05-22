<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - P2P Lending</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
</head>
<body>
<div class="admin-layout">
    <aside class="admin-sidebar">
        <h2>🏛️ ADMIN P2P</h2>
        <ul>
            <li class="${currentSection == 'ekyc' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=ekyc">1. Hồ sơ đăng ký (eKYC)</a>
            </li>
            <li class="${currentSection == 'investments' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=investments">2. Đầu tư (Duyệt góp vốn)</a>
            </li>
            <li class="${currentSection == 'loan_new' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=loan_new">3a. Gói vốn mới</a>
            </li>
            <li class="${currentSection == 'loan_current' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=loan_current">3b. Gói vốn hiện hành</a>
            </li>
            <li class="${currentSection == 'loan_expired' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=loan_expired">3c. Gói vốn hết hạn</a>
            </li>
            <li class="${currentSection == 'loan_processing' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=loan_processing">4. Gói vốn đang xử lý</a>
            </li>
            <li class="${currentSection == 'loan_closed' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=loan_closed">5. Gói vốn đã kết thúc</a>
            </li>
            <li class="${currentSection == 'users' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=users">6. Hồ sơ người dùng</a>
            </li>
        </ul>
        <form action="${pageContext.request.contextPath}/logout" method="POST" style="margin-top:24px;">
            <button type="submit" class="btn-admin btn-reject" style="width:100%;">Đăng xuất</button>
        </form>
    </aside>

    <div class="admin-main">
        <div class="admin-topbar">
            <strong>Bảng quản trị hệ thống</strong>
            <span>Admin: <c:out value="${adminEmail}"/></span>
        </div>
        <div class="admin-content">
            <c:if test="${not empty param.msg}">
                <div class="alert-admin success">Thao tác đã được ghi nhận (${param.msg}).</div>
            </c:if>

            <c:if test="${currentSection == 'ekyc'}">
                <h3>Hồ sơ đăng ký chờ duyệt eKYC</h3>
                <table class="admin-table">
                    <thead><tr><th>ID</th><th>Họ tên</th><th>Email</th><th>Role</th><th>Ngày ĐK</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="u" items="${pendingEkycList}">
                        <tr>
                            <td>${u.userId}</td>
                            <td>${u.fullName}</td>
                            <td>${u.email}</td>
                            <td>${u.role}</td>
                            <td><fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="approve_ekyc"/>
                                    <input type="hidden" name="userId" value="${u.userId}"/>
                                    <button class="btn-admin btn-approve">Duyệt</button>
                                </form>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="reject_ekyc"/>
                                    <input type="hidden" name="userId" value="${u.userId}"/>
                                    <button class="btn-admin btn-reject">Từ chối</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pendingEkycList}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có hồ sơ chờ duyệt.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'investments'}">
                <h3>Đầu tư chờ duyệt (pending)</h3>
                <table class="admin-table">
                    <thead><tr><th>ID</th><th>NĐT</th><th>Gói vay</th><th>Số tiền</th><th>Ngày</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="inv" items="${pendingInvestments}">
                        <tr>
                            <td>#${inv.investmentId}</td>
                            <td>${inv.investorName}</td>
                            <td>${inv.loanCode != null ? inv.loanCode : inv.loanId}</td>
                            <td><fmt:formatNumber value="${inv.amountInvested}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatDate value="${inv.investedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST">
                                    <input type="hidden" name="action" value="approve_investment"/>
                                    <input type="hidden" name="investmentId" value="${inv.investmentId}"/>
                                    <button class="btn-admin btn-approve">Duyệt Đầu tư</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'loan_new'}">
                <h3>Gói vốn mới (đơn vay pending)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã đơn</th><th>Borrower ID</th><th>Số tiền</th><th>Kỳ hạn</th><th>CIC</th><th>Duyệt</th></tr></thead>
                    <tbody>
                    <c:forEach var="app" items="${pendingLoanApps}">
                        <tr>
                            <td>#${app.applicationId}</td>
                            <td>${app.borrowerId}</td>
                            <td><fmt:formatNumber value="${app.amountRequested}" groupingUsed="true"/> đ</td>
                            <td>${app.termMonths} tháng</td>
                            <td><a href="${app.cicPdfUrl}" target="_blank">PDF</a></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="approve_loan"/>
                                    <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                    <input type="hidden" name="interestRate" value="12"/>
                                    <button class="btn-admin btn-approve">Duyệt lên sàn</button>
                                </form>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="reject_loan"/>
                                    <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                    <button class="btn-admin btn-reject">Từ chối</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'loan_current'}">
                <h3>Gói vốn hiện hành (đang gọi vốn)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Tổng vốn</th><th>Đã gom</th><th>Tiến độ</th><th>Giải ngân</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${currentFundingLoans}">
                        <c:set var="pct" value="${loan.fundingProgress}"/>
                        <tr>
                            <td>#${loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatNumber value="${loan.currentFunded}" groupingUsed="true"/> đ</td>
                            <td>
                                <div class="progress-bar-wrap"><div class="progress-bar-fill" style="width:${pct}%"></div></div>
                                <fmt:formatNumber value="${pct}" maxFractionDigits="1"/>%
                            </td>
                            <td>
                                <c:if test="${loan.fullyFunded}">
                                    <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST">
                                        <input type="hidden" name="action" value="disburse"/>
                                        <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                        <button class="btn-admin btn-primary">Giải ngân</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'loan_expired'}">
                <h3>Gói vốn hết hạn (không gọi đủ 100%)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Tổng</th><th>Đã gom</th><th>Trạng thái</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${expiredLoans}">
                        <tr>
                            <td>#${loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatNumber value="${loan.currentFunded}" groupingUsed="true"/> đ</td>
                            <td class="status-overdue">Hết hạn gọi vốn</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'loan_processing'}">
                <h3>Gói vốn đang xử lý</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Số tiền</th><th>Hạn trả</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${processingLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatDate value="${loan.dueDate}" pattern="dd/MM/yyyy"/></td>
                            <td class="status-processing">Đúng hạn</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/ContractServlet?loanId=${loan.loanId}" target="_blank" class="btn-admin btn-primary">Hợp đồng</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:forEach var="loan" items="${overdueLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatDate value="${loan.dueDate}" pattern="dd/MM/yyyy"/></td>
                            <td class="status-overdue">Quá hạn</td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="send_overdue_notice"/>
                                    <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                    <button class="btn-admin btn-warn">Gửi thông báo quá hạn</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:forEach var="loan" items="${awaitingCloseLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td>-</td>
                            <td class="status-completed">Đã thanh toán hết</td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST">
                                    <input type="hidden" name="action" value="close_loan"/>
                                    <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                    <button class="btn-admin btn-approve">Duyệt kết thúc</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'loan_closed'}">
                <h3>Gói vốn đã kết thúc (lịch sử)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Số tiền</th><th>Hợp đồng</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${closedLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td>${loan.borrowerName}</td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><a href="${pageContext.request.contextPath}/ContractServlet?loanId=${loan.loanId}">Xem</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${currentSection == 'users'}">
                <h3>Hồ sơ người dùng</h3>
                <div class="filter-bar">
                    <form method="GET" action="${pageContext.request.contextPath}/AdminDashboardServlet">
                        <input type="hidden" name="section" value="users"/>
                        <label>Lọc Role:</label>
                        <select name="roleFilter" onchange="this.form.submit()">
                            <option value="all" ${roleFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                            <option value="investor" ${roleFilter == 'investor' ? 'selected' : ''}>Investor</option>
                            <option value="borrower" ${roleFilter == 'borrower' ? 'selected' : ''}>Borrower</option>
                        </select>
                    </form>
                </div>
                <table class="admin-table">
                    <thead><tr><th>ID</th><th>Email</th><th>Role</th><th>Status</th><th>Chi tiết hồ sơ</th></tr></thead>
                    <tbody>
                    <c:forEach var="u" items="${userList}">
                        <tr>
                            <td>${u.userId}</td>
                            <td>${u.email}</td>
                            <td>${u.role}</td>
                            <td>${u.status}</td>
                            <td style="font-size:12px;">${u.profileDetail}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>
</div>
<footer class="site-footer">Email: admin@gmail.com - Số điện thoại: 01234567891</footer>
<button id="backToTop" title="Lên đầu trang">↑</button>
<script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>
</body>
</html>
