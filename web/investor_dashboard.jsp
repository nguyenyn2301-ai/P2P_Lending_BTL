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
        <div>
            <div class="sidebar-brand">🏛️ <span>P2P INVESTOR</span></div>
            <ul class="sidebar-menu">
                <li class="${empty currentAction || currentAction == 'dashboard' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=dashboard">Tổng Quan Main</a>
                </li>
                <li class="${currentAction == 'market' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=market">Gọi Vốn Vay</a>
                </li>
                <li class="${currentAction == 'profile' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=profile">Hồ Sơ Đăng Ký</a>
                </li>
                <li class="${currentAction == 'my_investments' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=my_investments">Lịch Sử Góp Vốn</a>
                </li>
            </ul>
        </div>
        <form action="${pageContext.request.contextPath}/logout" method="POST" style="margin:0;">
            <button type="submit" class="btn-logout">Đăng xuất</button>
        </form>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">
                <c:choose>
                    <c:when test="${currentAction == 'market'}">Gọi Vốn Vay — Gói trên sàn</c:when>
                    <c:when test="${currentAction == 'profile'}">Hồ Sơ Đăng Ký (eKYC)</c:when>
                    <c:when test="${currentAction == 'my_investments'}">Lịch Sử Góp Vốn</c:when>
                    <c:when test="${currentAction == 'deposit'}">Nạp tiền vào ví</c:when>
                    <c:otherwise>Tổng Quan Main</c:otherwise>
                </c:choose>
            </div>
            <div class="user-info" style="text-align: right;">
                <div>Xin chào, <strong>${investorName}</strong>
                <c:if test="${not empty investor}">
                    | Ví: <fmt:formatNumber value="${investor.walletBalance}" groupingUsed="true"/> đ
                    | Đóng băng: <fmt:formatNumber value="${investor.frozenBalance}" groupingUsed="true"/> đ
                </c:if></div>
                <div style="font-style: italic; color: #94a3b8; font-size: 12px; margin-top: 4px;">Liên lạc quản trị viện: Email: admin@gmail.com - Số điện thoại: 01234567891</div>
                <c:choose>
                    <c:when test="${trangThaiEkyc == 'verified'}">
                        <span class="badge badge-success">ĐÃ XÁC THỰC EKYC</span>
                    </c:when>
                    <c:when test="${trangThaiEkyc == 'rejected'}">
                        <span class="badge badge-danger">EKYC BỊ TỪ CHỐI</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-warning">CHỜ DUYỆT EKYC</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="container">
            <c:if test="${param.msg == 'pending'}"><div class="alert-banner alert-banner-success">Đã gửi yêu cầu góp vốn, chờ Admin duyệt.</div></c:if>
            <c:if test="${param.msg == 'insufficient'}"><div class="alert-banner alert-banner-danger">Số dư ví không đủ.</div></c:if>
            <c:if test="${param.msg == 'ekyc'}"><div class="alert-banner alert-banner-warning">Vui lòng hoàn tất eKYC và được duyệt trước khi góp vốn.</div></c:if>
            <c:if test="${param.msg == 'invalid'}"><div class="alert-banner alert-banner-danger">Gói vốn không tồn tại hoặc đã kết thúc gọi vốn.</div></c:if>
            <c:if test="${param.msg == 'ekyc_updated_success'}"><div class="alert-banner alert-banner-success">Hồ sơ eKYC đã gửi lại, chờ Admin duyệt.</div></c:if>
            <c:if test="${param.msg == 'deposit_pending'}"><div class="alert-banner alert-banner-success">Đã gửi yêu cầu nạp vốn. Vui lòng chờ Admin duyệt sau khi chuyển khoản.</div></c:if>
            <c:if test="${param.msg == 'deposit_pdf_required'}"><div class="alert-banner alert-banner-danger">Vui lòng chọn file đính kèm trước khi hoàn tất nạp vốn.</div></c:if>
            <c:if test="${param.msg == 'deposit_invalid'}"><div class="alert-banner alert-banner-danger">Số tiền nạp không hợp lệ.</div></c:if>
            <c:if test="${param.msg == 'deposit_fail'}"><div class="alert-banner alert-banner-danger">Không gửi được yêu cầu nạp tiền. Vui lòng thử lại.</div></c:if>

            <c:choose>
                <%-- TAB 1: TỔNG QUAN MAIN --%>
                <c:when test="${empty currentAction || currentAction == 'dashboard'}">
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-info">
                                <h5>SỐ DƯ VÍ KHẢ DỤNG</h5>
                                <h2><fmt:formatNumber value="${not empty investor.walletBalance ? investor.walletBalance : 0}" groupingUsed="true"/> đ</h2>
                                <p style="margin-top:12px;">
                                    <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=deposit"
                                       style="color:#2563eb;font-weight:600;text-decoration:underline;">Nạp tiền</a>
                                </p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-info">
                                <h5>VÍ ĐÓNG BĂNG</h5>
                                <h2 style="color:#f59e0b;"><fmt:formatNumber value="${not empty investor.frozenBalance ? investor.frozenBalance : 0}" groupingUsed="true"/> đ</h2>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-info">
                                <h5>KHẨU VỊ RỦI RO</h5>
                                <h2 style="font-size:18px;color:#3b82f6;"><c:out value="${not empty investor.riskAppetite ? investor.riskAppetite : '—'}"/></h2>
                            </div>
                        </div>
                    </div>
                    <div class="data-card">
                        <h4>Góp vốn gần đây</h4>
                        <table class="table-loan">
                            <thead>
                                <tr><th>Mã GD</th><th>Mã gói</th><th>Số tiền</th><th>Trạng thái</th><th>Thời gian</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="inv" items="${myInvestments}" begin="0" end="4">
                                <tr>
                                    <td>#${inv.investmentId}</td>
                                    <td>${not empty inv.loanCode ? inv.loanCode : inv.loanId}</td>
                                    <td><fmt:formatNumber value="${inv.amountInvested}" groupingUsed="true"/> đ</td>
                                    <td><c:out value="${inv.status}"/></td>
                                    <td><fmt:formatDate value="${inv.investedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myInvestments}">
                                <tr><td colspan="5" style="text-align:center;color:#94a3b8;">Chưa có giao dịch góp vốn.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                        <p style="margin-top:12px;">
                            <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=market" style="color:#2563eb;">→ Vào sàn Gọi Vốn Vay</a>
                        </p>
                    </div>
                </c:when>

                <%-- TAB 2: GỌI VỐN VAY (SÀN) --%>
                <c:when test="${currentAction == 'market'}">
                    <div class="data-card">
                        <h4>Gói vay trên sàn</h4>
                        <table class="table-loan">
                            <thead>
                                <tr><th>Mã</th><th>Người vay</th><th>Lãi suất</th><th>Số tiền</th><th>Tiến độ</th><th>Chi tiết</th><th>Góp vốn</th></tr>
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
                                        <a href="${pageContext.request.contextPath}/InvestorLoanDetailServlet?loanId=${loan.loanId}"
                                           style="color:#2563eb;text-decoration:underline;font-size:13px;white-space:nowrap;">Xem thêm chi tiết</a>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${trangThaiEkyc == 'verified'}">
                                                <form action="${pageContext.request.contextPath}/InvestmentController" method="POST" style="display:flex;gap:4px;flex-wrap:wrap;">
                                                    <input type="hidden" name="loanId" value="${loan.loanId}"/>
                                                    <input type="number" name="amount" min="100000" step="100000" placeholder="Số tiền" required style="width:120px;padding:6px;"/>
                                                    <input type="text" name="bankName" placeholder="Ngân hàng" required style="width:100px;padding:6px;"/>
                                                    <input type="text" name="accountNumber" placeholder="STK" required style="width:100px;padding:6px;"/>
                                                    <button type="submit" class="btn-submit" style="width:auto;padding:6px 12px;">Góp vốn</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color:#94a3b8;font-size:12px;">Cần eKYC verified</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty fundingLoans}">
                                <tr><td colspan="7" style="text-align:center;color:#94a3b8;">Không có gói đang gọi vốn.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </c:when>

                <%-- TAB 3: HỒ SƠ ĐĂNG KÝ --%>
                <c:when test="${currentAction == 'profile'}">
                    <div class="data-card" style="max-width:640px;">
                        <h4>Hồ sơ đăng ký nhà đầu tư</h4>
                        <table class="table-loan">
                            <tbody>
                                <tr><th style="width:200px;">Họ tên</th><td><c:out value="${investorName}"/></td></tr>
                                <tr><th>Email</th><td><c:out value="${investor.email}"/></td></tr>
                                <tr><th>Khẩu vị rủi ro</th><td><c:out value="${investor.riskAppetite}"/></td></tr>
                                <tr><th>Trạng thái eKYC</th>
                                    <td>
                                        <c:choose>
                                            <c:when test="${trangThaiEkyc == 'verified'}">Đã xác thực</c:when>
                                            <c:when test="${trangThaiEkyc == 'rejected'}">Bị từ chối</c:when>
                                            <c:otherwise>Chờ duyệt</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <p style="margin-top:16px;">
                            <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=re_ekyc"
                               class="btn-submit" style="display:inline-block;width:auto;padding:10px 20px;text-decoration:none;">
                                Cập nhật hồ sơ eKYC
                            </a>
                        </p>
                    </div>
                </c:when>

                <%-- TAB: NẠP TIỀN --%>
                <c:when test="${currentAction == 'deposit'}">
                    <div class="data-card" style="max-width: 600px; margin: 0 auto;">
                        <h4>Nạp tiền vào ví nhà đầu tư</h4>
                        <p style="color:#64748b;font-size:14px;margin-bottom:16px;">
                            Chuyển khoản tới tài khoản Admin, sau đó nhập số tiền đã chuyển và tải PDF CIC để Admin duyệt cộng vào số dư khả dụng.
                        </p>
                        <form action="${pageContext.request.contextPath}/InvestorDashboardServlet" method="POST"
                              enctype="multipart/form-data">
                            <input type="hidden" name="action" value="submit_deposit"/>

                            <div class="form-group">
                                <label>Số tài khoản nhận (Admin)</label>
                                <input type="text" class="form-control" value="${adminBankAccount}" readonly>
                            </div>
                            <div class="form-group">
                                <label>Ngân hàng</label>
                                <input type="text" class="form-control" value="${adminBankName}" readonly>
                            </div>
                            <div class="form-group">
                                <label for="depositAmount">Số tiền đã nạp (VNĐ) <span style="color:red;">*</span></label>
                                <input type="number" id="depositAmount" name="depositAmount" min="10000" step="1000"
                                       class="form-control" placeholder="Nhập số tiền bạn đã chuyển khoản" required>
                            </div>
                            <div class="form-group">
                                <label>Nội dung chuyển khoản (tự động)</label>
                                <input type="text" class="form-control" value="${transferContent}" readonly
                                       style="background:#f1f5f9;font-weight:600;">
                                <span class="form-hint">Vui lòng ghi đúng nội dung này khi chuyển khoản.</span>
                            </div>
                            <div class="form-group">
                                <label for="cicPdfInvestor">Tải lên file PDF đính kèm <span style="color:red;">*</span></label>
                                <input type="file" id="cicPdfInvestor" name="cicPdfUrl" class="form-control" required>
                            </div>
                            <button type="submit" class="btn-submit">Hoàn tất nạp vốn</button>
                        </form>
                        <p style="margin-top:16px;">
                            <a href="${pageContext.request.contextPath}/InvestorDashboardServlet?action=dashboard" style="color:#64748b;">← Quay lại Tổng quan</a>
                        </p>
                    </div>
                </c:when>

                <%-- TAB 4: LỊCH SỬ GÓP VỐN --%>
                <c:when test="${currentAction == 'my_investments'}">
                    <div class="data-card">
                        <h4>Lịch sử góp vốn của bạn</h4>
                        <table class="table-loan">
                            <thead>
                                <tr><th>Mã GD</th><th>Mã gói / Loan</th><th>Số tiền góp</th><th>Trạng thái</th><th>Ngày góp</th><th>Chi tiết gói</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="inv" items="${myInvestments}">
                                <tr>
                                    <td>#${inv.investmentId}</td>
                                    <td>${not empty inv.loanCode ? inv.loanCode : inv.loanId}</td>
                                    <td><fmt:formatNumber value="${inv.amountInvested}" groupingUsed="true"/> đ</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${inv.status == 'pending'}"><span style="color:#a16207;">Chờ duyệt</span></c:when>
                                            <c:when test="${inv.status == 'completed'}"><span style="color:#16a34a;">Hoàn tất</span></c:when>
                                            <c:otherwise><c:out value="${inv.status}"/></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${inv.investedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/InvestorLoanDetailServlet?loanId=${inv.loanId}"
                                           style="color:#2563eb;text-decoration:underline;">Xem PDF hồ sơ</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty myInvestments}">
                                <tr><td colspan="6" style="text-align:center;color:#94a3b8;">Chưa có lịch sử góp vốn.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </c:when>
            </c:choose>
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
