<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - P2P Lending</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
</head>
<body class="admin-with-notif">
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
            <li class="${currentSection == 'investor_deposits' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=investor_deposits">2b. Nạp tiền nhà đầu tư</a>
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
            <button type="submit" class="btn-logout">Đăng xuất</button>
        </form>
    </aside>

    <div class="admin-main">
        <div class="admin-topbar" style="display: flex; justify-content: space-between; align-items: flex-end;">
            <strong>Bảng quản trị hệ thống</strong>
            <div style="text-align: right;">
                <div>Admin: <c:out value="${adminEmail}"/></div>
                <div style="font-style: italic; color: #94a3b8; font-size: 12px; margin-top: 4px;">Liên lạc quản trị viện: Email: admin@gmail.com - Số điện thoại: 01234567891</div>
            </div>
        </div>
        
        <div class="admin-content">
            <c:if test="${not empty param.msg}">
                <div class="alert-admin success">Thao tác đã được ghi nhận (${param.msg}).</div>
            </c:if>

            <%-- 1. SECTION: EKYC --%>
            <c:if test="${currentSection == 'ekyc'}">
                <h3>Hồ sơ đăng ký chờ duyệt eKYC</h3>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th><th>Họ tên</th><th>Email</th><th>Role</th><th>Ngày ĐK</th>
                            <th>Tài liệu</th><th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${pendingEkycList}">
                        <tr>
                            <td>${u.userId}</td>
                            <td><c:out value="${u.fullName}"/></td>
                            <td><c:out value="${u.email}"/></td>
                            <td>${u.role}</td>
                            <td><fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty u.frontImg or not empty u.backImg or not empty u.selfieImg}">
                                        <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=documents&amp;userId=${u.userId}"
                                           class="btn-admin btn-primary" style="font-size:12px;padding:4px 8px;">Xem chi tiết</a>
                                    </c:when>
                                    <c:otherwise><span class="ekyc-missing">Chưa có</span></c:otherwise>
                                </c:choose>
                            </td>
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
                        <tr><td colspan="7" style="text-align:center;color:#94a3b8;">Không có hồ sơ eKYC nào đang chờ duyệt.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 2b. SECTION: INVESTOR DEPOSITS --%>
            <c:if test="${currentSection == 'investor_deposits'}">
                <h3>Yêu cầu nạp tiền nhà đầu tư (chờ duyệt)</h3>
                <table class="admin-table">
                    <thead>
                        <tr><th>ID</th><th>Nhà đầu tư</th><th>Số tiền</th><th>Nội dung CK</th><th>Ngày</th><th>Thao tác</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="dep" items="${pendingDeposits}">
                        <tr>
                            <td>#${dep.depositId}</td>
                            <td><c:out value="${dep.investorName}"/></td>
                            <td><fmt:formatNumber value="${dep.amount}" groupingUsed="true"/> đ</td>
                            <td style="font-size:12px;"><c:out value="${dep.transferContent}"/></td>
                            <td><fmt:formatDate value="${dep.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="approve_deposit"/>
                                    <input type="hidden" name="depositId" value="${dep.depositId}"/>
                                    <button class="btn-admin btn-approve">Duyệt nạp tiền</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pendingDeposits}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có yêu cầu nạp tiền nào chờ duyệt. Xem PDF CIC qua «Xem thêm» ở cột thông báo bên phải.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 2. SECTION: INVESTMENTS --%>
            <c:if test="${currentSection == 'investments'}">
                <h3>Đầu tư chờ duyệt (pending)</h3>
                <table class="admin-table">
                    <thead><tr><th>ID</th><th>NĐT</th><th>Gói vay</th><th>Số tiền</th><th>Ngày</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="inv" items="${pendingInvestments}">
                        <tr>
                            <td>#${inv.investmentId}</td>
                            <td><c:out value="${inv.investorName}"/></td>
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
                    <c:if test="${empty pendingInvestments}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có giao dịch đầu tư nào chờ duyệt.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 3a. SECTION: LOAN NEW (PENDING APPLICATIONS) --%>
            <c:if test="${currentSection == 'loan_new'}">
                <h3>Gói vốn mới (đơn vay pending)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã đơn</th><th>Borrower ID</th><th>Số tiền</th><th>Kỳ hạn</th><th>PDF đính kèm</th><th>Duyệt</th></tr></thead>
                    <tbody>
                    <c:forEach var="app" items="${pendingLoanApps}">
                        <tr>
                            <td>#${app.applicationId}</td>
                            <td>${app.borrowerId}</td>
                            <td><fmt:formatNumber value="${app.amountRequested}" groupingUsed="true"/> đ</td>
                            <td>${app.termMonths} tháng</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty app.cicPdfUrl}">
                                        <c:set var="pdfHref" value="${fn:startsWith(app.cicPdfUrl, 'http://') or fn:startsWith(app.cicPdfUrl, 'https://') ? app.cicPdfUrl : pageContext.request.contextPath.concat('/uploads/').concat(app.cicPdfUrl)}"/>
                                        <a href="${pdfHref}" target="_blank" rel="noopener" style="color: #3b82f6; text-decoration: underline;">PDF</a>
                                    </c:when>
                                    <c:otherwise><span class="ekyc-missing">Chưa có</span></c:otherwise>
                                </c:choose>
                            </td>
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
                    <c:if test="${empty pendingLoanApps}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có đơn yêu cầu vay vốn nào mới.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 3b. SECTION: LOAN CURRENT --%>
            <c:if test="${currentSection == 'loan_current'}">
                <h3>Gói vốn hiện hành (đang gọi vốn)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Tổng vốn</th><th>Đã gom</th><th>Tiến độ</th><th>Giải ngân</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${currentFundingLoans}">
                        <c:set var="pct" value="${loan.fundingProgress}"/>
                        <tr>
                            <td>#${loan.loanId}</td>
                            <td><c:out value="${loan.borrowerName}"/></td>
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
                                <c:if test="${not loan.fullyFunded}">
                                    <span style="color:#64748b; font-size:12px;">Đang gom vốn...</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty currentFundingLoans}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có gói vốn nào đang gọi trên sàn.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 3c. SECTION: LOAN EXPIRED --%>
            <c:if test="${currentSection == 'loan_expired'}">
                <h3>Gói vốn hết hạn (không gọi đủ 100%)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Tổng</th><th>Đã gom</th><th>Trạng thái</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${expiredLoans}">
                        <tr>
                            <td>#${loan.loanId}</td>
                            <td><c:out value="${loan.borrowerName}"/></td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><fmt:formatNumber value="${loan.currentFunded}" groupingUsed="true"/> đ</td>
                            <td class="status-overdue">Hết hạn gọi vốn</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty expiredLoans}">
                        <tr><td colspan="5" style="text-align:center;color:#94a3b8;">Không có gói vốn nào bị hết hạn gom.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 4. SECTION: LOAN PROCESSING --%>
            <c:if test="${currentSection == 'loan_processing'}">
                <h3>Gói vốn đang xử lý</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Số tiền</th><th>Hạn trả</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${processingLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td><c:out value="${loan.borrowerName}"/></td>
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
                            <td><c:out value="${loan.borrowerName}"/></td>
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
                            <td><c:out value="${loan.borrowerName}"/></td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td>-</td>
                            <td class="status-completed">Đã tất toán</td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST">
                                    <input type="hidden" name="action" value="close_loan"/>
                                    <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                    <button class="btn-admin btn-approve">Duyệt kết thúc</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty processingLoans && empty overdueLoans && empty awaitingCloseLoans}">
                        <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Không có gói vốn nào đang trong quá trình xử lý/vận hành.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 5. SECTION: LOAN CLOSED --%>
            <c:if test="${currentSection == 'loan_closed'}">
                <h3>Gói vốn đã kết thúc (lịch sử)</h3>
                <table class="admin-table">
                    <thead><tr><th>Mã</th><th>Người vay</th><th>Số tiền</th><th>Hợp đồng</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${closedLoans}">
                        <tr>
                            <td>${loan.loanCode != null ? loan.loanCode : loan.loanId}</td>
                            <td><c:out value="${loan.borrowerName}"/></td>
                            <td><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> đ</td>
                            <td><a href="${pageContext.request.contextPath}/ContractServlet?loanId=${loan.loanId}" style="color: #3b82f6;">Xem</a></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty closedLoans}">
                        <tr><td colspan="4" style="text-align:center;color:#94a3b8;">Chưa có lịch sử gói vốn nào kết thúc.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- 6. SECTION: USERS (QUẢN LÝ TÀI KHOẢN) --%>
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
                    <thead><tr><th>ID</th><th>Email</th><th>Role</th><th>Status</th><th>Chi tiết hồ sơ</th><th>Tài liệu</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <c:forEach var="u" items="${userList}">
                        <tr>
                            <td>${u.userId}</td>
                            <td><c:out value="${u.email}"/></td>
                            <td>${u.role}</td>
                            <td>
                                <span class="badge-${u.status == 'active' ? 'success' : (u.status == 'pending' ? 'warn' : 'danger')}">
                                    ${u.status}
                                </span>
                            </td>
                            <td style="font-size:12px;"><c:out value="${u.profileDetail}"/></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/AdminDashboardServlet?section=documents&amp;userId=${u.userId}"
                                   style="font-size:12px;color:#3b82f6;">Xem chi tiết</a>
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/AdminActionServlet" method="POST"
                                      onsubmit="return confirm('Xóa vĩnh viễn tài khoản ${u.email}? Hành động không hoàn tác.');">
                                    <input type="hidden" name="action" value="delete_user"/>
                                    <input type="hidden" name="userId" value="${u.userId}"/>
                                    <button type="submit" class="btn-admin btn-reject">Xóa tài khoản</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty userList}">
                        <tr><td colspan="7" style="text-align:center;color:#94a3b8;">Không tìm thấy người dùng phù hợp với bộ lọc.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>

            <%-- Xem tài liệu eKYC / PDF theo user (tải khi admin bấm Xem chi tiết) --%>
            <c:if test="${currentSection == 'documents'}">
                <p><a href="javascript:history.back()" style="color:#3b82f6;">← Quay lại</a></p>
                <h3>Tài liệu người dùng #${docUserId}</h3>

                <div class="data-card" style="margin-bottom:16px;">
                    <h4 style="margin-top:0;">eKYC (CCCD &amp; chân dung)</h4>
                    <c:choose>
                        <c:when test="${not empty ekycDocList}">
                            <div class="doc-preview-grid">
                                <c:forEach var="doc" items="${ekycDocList}">
                                    <div class="doc-preview-item">
                                        <div class="doc-preview-label">${doc.documentType}</div>
                                        <c:set var="imgUrl" value="${pageContext.request.contextPath}/uploads/${doc.fileUrl}"/>
                                        <a href="${imgUrl}" target="_blank" rel="noopener">
                                            <img src="${imgUrl}" alt="${doc.documentType}" class="ekyc-thumb" loading="lazy"/>
                                        </a>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise><span class="ekyc-missing">Chưa có ảnh eKYC.</span></c:otherwise>
                    </c:choose>
                </div>

                <div class="data-card">
                    <h4 style="margin-top:0;">PDF đơn đăng ký vay</h4>
                    <c:choose>
                        <c:when test="${not empty loanPdfList}">
                            <table class="admin-table">
                                <thead><tr><th>Mã đơn</th><th>Trạng thái</th><th>Ngày</th><th>File</th></tr></thead>
                                <tbody>
                                <c:forEach var="lp" items="${loanPdfList}">
                                    <tr>
                                        <td>#${lp.applicationId}</td>
                                        <td>${lp.status}</td>
                                        <td><fmt:formatDate value="${lp.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>
                                            <c:set var="loanPdfHref" value="${pageContext.request.contextPath}/uploads/${lp.fileUrl}"/>
                                            <a href="${loanPdfHref}" target="_blank" rel="noopener">Mở PDF</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise><span class="ekyc-missing">Chưa có PDF đơn vay.</span></c:otherwise>
                    </c:choose>
                </div>
            </c:if>
        </div>
    </div>

    <aside class="notification-panel">
        <h4>🔔 Thông báo</h4>
        <c:forEach var="n" items="${adminNotifications}">
            <div class="notif-item ${n.read ? '' : 'unread'}">
                <h5>${n.title}</h5>
                <p>${n.message}</p>
                <c:if test="${not empty n.linkUrl}">
                    <p style="margin-top:6px;">
                        <a href="${pageContext.request.contextPath}/uploads/${n.linkUrl}" target="_blank" rel="noopener"
                           style="color:#2563eb;font-weight:600;text-decoration:underline;">Xem thêm</a>
                    </p>
                </c:if>
                <small><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </div>
        </c:forEach>
        <c:if test="${empty adminNotifications}">
            <p style="color:#94a3b8;font-size:13px;">Chưa có thông báo.</p>
        </c:if>
    </aside>
</div>

<button id="backToTop" title="Lên đầu trang">↑</button>
<script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>
</body>
</html>