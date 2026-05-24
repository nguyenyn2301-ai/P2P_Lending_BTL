<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sàn P2P Lending - Bảng Điều Khiển</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout-common.css">
    <style>
        /* Tối ưu nhỏ để hộp preview không bị lỗi hiển thị ảnh trống */
        .preview-box { display: none; margin-top: 8px; max-width: 100%; border: 1px dashed #cbd5e1; padding: 5px; border-radius: 6px; }
        .preview-box img { max-width: 100%; height: auto; display: block; border-radius: 4px; }
    </style>
</head>
<body class="dashboard-wrapper">

    <div class="sidebar">
        <div>
            <div class="sidebar-brand">🏛️ <span>P2P LENDING</span></div>
            <ul class="sidebar-menu">
                <li class="${empty currentAction || currentAction == 'dashboard' ? 'active' : ''}" id="menu-dashboard">
                    <a href="${pageContext.request.contextPath}/BorrowerDashboardServlet?action=dashboard">Tổng Quan Main</a>
                </li>
                
                <c:choose>
                    <c:when test="${trangThaiEkyc == 'rejected'}">
                        <li class="disabled-menu" onclick="alert('Không thể đăng ký: Hồ sơ eKYC của bạn đã bị từ chối! Vui lòng thực hiện cập nhật lại hồ sơ.')">
                            <a href="javascript:void(0);">Đăng Ký Vay Mới</a>
                        </li>
                    </c:when>
                    <c:when test="${hasOverdue}">
                        <li class="disabled-menu" onclick="alert('Không thể đăng ký: Bạn có khoản vay quá hạn! Vui lòng thanh toán tại mục Trả nợ.')">
                            <a href="javascript:void(0);">Đăng Ký Vay Mới (Quá hạn)</a>
                        </li>
                    </c:when>
                    <c:when test="${hasActiveLoan}">
                        <li class="disabled-menu" onclick="alert('Không thể đăng ký: Bạn đang có một đơn vay chưa tất toán hoặc đang chờ duyệt!')">
                            <a href="javascript:void(0);">Đăng Ký Vay Mới</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="${currentAction == 'create_loan' ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/BorrowerDashboardServlet?action=create_loan">Đăng Ký Vay Mới</a>
                        </li>
                    </c:otherwise>
                </c:choose>

                <li class="${currentAction == 'market_loans' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/BorrowerDashboardServlet?action=market_loans">Khoản Vay Trên Sàn</a>
                </li>
                <li class="${currentAction == 'repayment' ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/RepaymentServlet">Trả Nợ Theo Kỳ</a>
                </li>
            </ul>
        </div>
        <form action="${pageContext.request.contextPath}/logout" method="POST" style="margin:0;">
            <button type="submit" class="btn-logout">Đăng xuất</button>
        </form>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title" id="dynamic-topbar-title">
                <c:choose>
                    <c:when test="${currentAction == 'create_loan'}">Đăng Ký Khoản Vay Mới</c:when>
                    <c:when test="${currentAction == 'market_loans'}">Khoản Vay Đang Gọi Vốn Toàn Sàn</c:when>
                    <c:when test="${currentAction == 're_ekyc'}">Cập Nhật Thông Tin Định Danh eKYC</c:when>
                    <c:otherwise>Bảng Điều Khiển Tổng Overview</c:otherwise>
                </c:choose>
            </div>
            <div class="user-info" style="text-align: right;">
                <div><span>Xin chào, <strong><c:out value="${not empty borrowerName ? borrowerName : 'Người dùng'}"/></strong></span></div>
                <div style="font-style: italic; color: #94a3b8; font-size: 12px; margin-top: 4px;">Liên lạc quản trị viện: Email: admin@gmail.com - Số điện thoại: 01234567891</div>
                <c:choose>
                    <c:when test="${trangThaiEkyc == 'verified'}">
                        <span class="badge badge-success">ĐÃ XÁC THỰC EKYC</span>
                    </c:when>
                    <c:when test="${trangThaiEkyc == 'rejected'}">
                        <span class="badge badge-danger" title="Hồ sơ định danh bị lỗi" style="cursor:help;">⚠️ EKYC BỊ TỪ CHỐI</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-warning">CHỜ DUYỆT EKYC</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="container">
            
            <%-- THÔNG BÁO HỆ THỐNG --%>
            <c:if test="${param.msg == 'ekyc_updated_success'}">
                <div class="alert-banner alert-banner-success">
                    <strong>🎉 Thành công:</strong> Hồ sơ eKYC của bạn đã được cập nhật thành công. Trạng thái chuyển về <b>Chờ duyệt (Pending)</b>.
                </div>
            </c:if>

            <c:if test="${param.msg == 'ekyc_updated_failed'}">
                <div class="alert-banner alert-banner-danger">
                    <strong>Thất bại:</strong> Đã xảy ra lỗi khi upload hoặc cập nhật thông tin hồ sơ eKYC. Vui lòng thử lại.
                </div>
            </c:if>

            <c:if test="${param.msg == 'loan_submit_success'}">
                <div class="alert-banner alert-banner-success">
                    <strong>Đăng ký thành công:</strong> Đơn vay đã tiếp nhận sang trạng thái <b>Chờ duyệt</b> để thẩm định tệp hồ sơ CIC PDF.
                </div>
            </c:if>

            <c:if test="${param.msg == 'loan_submit_failed'}">
                <div class="alert-banner alert-banner-danger">
                    <strong>Đăng ký thất bại:</strong> Không lưu được đơn vay. Vui lòng thử lại hoặc liên hệ quản trị viên.
                </div>
            </c:if>

            <c:if test="${param.msg == 'loan_pdf_required'}">
                <div class="alert-banner alert-banner-danger">
                    <strong>Thiếu file đính kèm:</strong> Vui lòng chọn file trước khi gửi đơn gọi vốn.
                </div>
            </c:if>

            <c:if test="${param.msg == 'error_ekyc_rejected' || (trangThaiEkyc == 'rejected' && currentAction != 're_ekyc')}">
                <div class="alert-banner alert-banner-danger" id="rejected-warning-banner">
                    <strong>Quyền truy cập bị hạn chế:</strong> Hồ sơ định danh cá nhân (eKYC) của bạn hiện đang ở trạng thái <b>Từ chối (Rejected)</b>.
                    <br>
                    <a href="${pageContext.request.contextPath}/BorrowerDashboardServlet?action=re_ekyc" class="btn-action-ekyc">🔄 Cập nhật lại thông tin eKYC ngay</a>
                </div>
            </c:if>
            
            <c:if test="${param.msg == 'error_already_has_loan' || (hasActiveLoan && (empty currentAction || currentAction == 'dashboard'))}">
                <div class="alert-banner alert-banner-warning">
                    <strong>ℹ️ Một gói gọi vốn tại một thời điểm:</strong> Bạn đang có gói vốn chưa gọi đủ 100%, chưa kết thúc hoặc chưa trả nợ xong. Vui lòng hoàn tất gói hiện tại trước khi đăng ký gói mới.
                </div>
            </c:if>

            <c:choose>
                <%-- TAB 1: TỔNG QUAN MAIN --%>
                <c:when test="${empty currentAction || currentAction == 'dashboard'}">
                    <div id="main-dashboard-view">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-info">
                                    <h5>HẠN MỨC VAY TỐI ĐA</h5>
                                    <h2><fmt:formatNumber value="${not empty hanMucToiDa ? hanMucToiDa : 0}" type="number" groupingUsed="true"/> đ</h2>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-info">
                                    <h5>TỔNG DƯ NỢ HIỆN TẠI</h5>
                                    <h2 style="color: #ef4444;"><fmt:formatNumber value="${not empty tongDuNo ? tongDuNo : 0}" type="number" groupingUsed="true"/> đ</h2>
                                </div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-info">
                                    <h5>THU NHẬP KÊ KHAI</h5>
                                    <h2 style="color: #10b981;"><fmt:formatNumber value="${not empty thuNhapKhai ? thuNhapKhai : 0}" type="number" groupingUsed="true"/> đ</h2>
                                </div>
                            </div>
                        </div>

                        <div class="data-card">
                            <h4>📄 Hồ Sơ Giao Dịch Khoản Vay Của Bạn</h4>
                            <table class="table-loan">
                                <thead>
                                    <tr>
                                        <th>MÃ ĐƠN VAY</th>
                                        <th>SỐ TIỀN ĐĂNG KÝ</th>
                                        <th>KỲ HẠN VAY</th>
                                        <th>NGÀY TẠO ĐƠN</th>
                                        <th style="text-align: center;">XEM CIC</th>
                                        <th style="text-align: center;">HỢP ĐỒNG</th>
                                        <th>TRẠNG THÁI HỒ SƠ</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty myLoansList}">
                                            <c:forEach var="myLoan" items="${myLoansList}">
                                                <tr>
                                                    <td>#<strong><c:out value="${myLoan.applicationId}"/></strong></td>
                                                    <td><strong><fmt:formatNumber value="${myLoan.amountRequested}" type="number" groupingUsed="true"/> đ</strong></td>
                                                    <td><c:out value="${myLoan.termMonths}"/> Tháng</td>
                                                    <td><fmt:formatDate value="${myLoan.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                    <td style="text-align: center;">
                                                        <c:choose>
                                                            <c:when test="${not empty myLoan.cicPdfUrl}">
                                                                <c:set var="myPdfHref" value="${fn:startsWith(myLoan.cicPdfUrl, 'http://') or fn:startsWith(myLoan.cicPdfUrl, 'https://') ? myLoan.cicPdfUrl : pageContext.request.contextPath.concat('/uploads/').concat(myLoan.cicPdfUrl)}"/>
                                                                <a href="${myPdfHref}" target="_blank" rel="noopener" style="color:#2563eb; text-decoration:underline; font-weight: bold;">📄 Xem PDF</a>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span style="color: #94a3b8; font-style: italic; font-size: 13px;">Chưa có file</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <c:choose>
                                                            <c:when test="${myLoan.loanId > 0}">
                                                                <a href="${pageContext.request.contextPath}/ContractServlet?loanId=${myLoan.loanId}"
                                                                   target="_blank" rel="noopener"
                                                                   style="color:#2563eb; text-decoration:underline; font-weight:bold;">Hợp đồng</a>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span style="color:#94a3b8; font-size:13px;">—</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${myLoan.status == 'Chờ duyệt'}"><span style="color:#a16207; font-weight:600;">Chờ duyệt</span></c:when>
                                                            <c:when test="${myLoan.status == 'Đã duyệt'}"><span style="color:#2563eb; font-weight:600;">Đã duyệt</span></c:when>
                                                            <c:when test="${myLoan.status == 'Đang gọi vốn'}"><span style="color:#16a34a; font-weight:600;">Đang gọi vốn</span></c:when>
                                                            <c:otherwise><span style="color:#dc2626; font-weight:600;"><c:out value="${myLoan.status}"/></span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="7" style="text-align: center; color: #94a3b8; padding: 25px;">Bạn chưa có đơn đăng ký vay cá nhân nào.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:when>
                <c:when test="${currentAction == 're_ekyc'}">
                    <div class="data-card" style="max-width: 600px; margin: 0 auto;">
                        <h4>🔄 Làm mới hồ sơ định danh cá nhân (eKYC)</h4>
                        
                        <form action="${pageContext.request.contextPath}/BorrowerDashboardServlet" method="POST" enctype="multipart/form-data">
                            <input type="hidden" name="action" value="update_ekyc">
                            
                            <div class="form-group">
                                <label>Họ và Tên đệm <span style="color:red;">*</span></label>
                                <input type="text" name="firstName" class="form-control" value="${not empty borrowerObj.firstName ? borrowerObj.firstName : ''}" required>
                            </div>

                            <div class="form-group">
                                <label>Tên <span style="color:red;">*</span></label>
                                <input type="text" name="lastName" class="form-control" value="${not empty borrowerObj.lastName ? borrowerObj.lastName : ''}" required>
                            </div>

                            <div class="form-group">
                                <label>Số CCCD / CMND mới <span style="color:red;">*</span></label>
                                <input type="text" name="idCardNumber" class="form-control" value="${not empty borrowerObj.idCardNumber ? borrowerObj.idCardNumber : ''}" required>
                            </div>

                            <div class="form-group">
                                <label>Ảnh mặt trước CCCD / CMND <span style="color:red;">*</span></label>
                                <input type="file" name="cccd_front" class="form-control" accept="image/*" required onchange="previewImage(this, 'front_preview')">
                                <div id="front_preview" class="preview-box"><img src="" alt="Preview"></div>
                            </div>

                            <div class="form-group">
                                <label>Ảnh mặt sau CCCD / CMND <span style="color:red;">*</span></label>
                                <input type="file" name="cccd_back" class="form-control" accept="image/*" required onchange="previewImage(this, 'back_preview')">
                                <div id="back_preview" class="preview-box"><img src="" alt="Preview"></div>
                            </div>

                            <div class="form-group">
                                <label>Ảnh chân dung chụp cùng CCCD (Selfie) <span style="color:red;">*</span></label>
                                <input type="file" name="selfie_avatar" class="form-control" accept="image/*" required onchange="previewImage(this, 'selfie_preview')">
                                <div id="selfie_preview" class="preview-box"><img src="" alt="Preview"></div>
                            </div>

                            <div class="form-group">
                                <label>Thu nhập hằng tháng kê khai (VNĐ)</label>
                                <input type="number" name="monthlyIncome" class="form-control" value="${not empty borrowerObj.monthlyIncome ? borrowerObj.monthlyIncome : ''}" required oninput="previewCurrencyUpdate(this.value)">
                                <div id="updateCurrencyPreview" class="currency-preview"></div>
                            </div>
                            
                            <button type="submit" class="btn-submit" style="background-color: #f59e0b; color: #0f172a; font-weight: bold; cursor: pointer;">🚀 Gửi lại hồ sơ kiểm duyệt</button>
                        </form>
                    </div>
                </c:when>

                <%-- TAB 2: ĐĂNG KÝ VAY CHUẨN LOAN_APPLICATION TABLE --%>
                <c:when test="${currentAction == 'create_loan'}">
                    <c:choose>
                        <c:when test="${trangThaiEkyc == 'verified' && !hasActiveLoan}">
                            <div class="data-card" style="max-width: 600px; margin: 0 auto;">
                                <h4>Tạo Đơn Đăng Ký Vay Mới</h4>
                                
                                <form action="${pageContext.request.contextPath}/BorrowerDashboardServlet" method="POST" id="loanForm" onsubmit="return validateLoanAmount()" enctype="multipart/form-data">
                                    <input type="hidden" name="action" value="submit_loan">
                                    
                                    <div class="form-group">
                                        <label for="soTienVay">Số tiền yêu cầu gọi vốn (VNĐ)</label>
                                        <input type="number" id="soTienVay" name="amountRequested" min="1000000" max="${not empty hanMucToiDa ? hanMucToiDa : 0}" data-max="${not empty hanMucToiDa ? hanMucToiDa : 0}" class="form-control" required oninput="previewCurrency(this.value)">
                                        <div id="currencyPreview" class="currency-preview"></div>
                                        <span class="form-hint">Hạn mức tối đa được phép vay: <strong style="color: var(--primary-color);"><fmt:formatNumber value="${hanMucToiDa}" type="number"/> đ</strong></span>
                                    </div>

                                    <div class="form-group">
                                        <label for="kyHan">Kỳ hạn vay (Tháng)</label>
                                        <input type="number" id="kyHan" name="termMonths" min="1" max="60" placeholder="Ví dụ: 6, 12, 24" class="form-control" required>
                                    </div>

                                    <div class="form-group">
                                        <label for="ngayCapCic">Ngày phát hành tài liệu đính kèm</label>
                                        <input type="date" id="ngayCapCic" name="cicIssuedDate" class="form-control" required>
                                    </div>

                                    <div class="form-group">
                                        <label for="urlFileCic">Tải lên file PDF đính kèm <span style="color:red;">*</span></label>
                                        <input type="file" id="urlFileCic" name="cicPdfUrl" class="form-control" required>
                             
                                    </div>

                                    <button type="submit" class="btn-submit">Tạo gói gọi vốn / Gửi đơn vay</button>
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert-lock">
                                <span style="font-size: 40px;">🔒</span>
                                <h3>Chức năng đăng ký vay đã bị khóa</h3>
                                <p>Tài khoản chưa xác thực eKYC hoặc bạn đang có gói gọi vốn chưa hoàn tất (chờ duyệt / đang gọi vốn / đang trả nợ).</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>

                <%-- TAB 3: KHOẢN VAY TRÊN SÀN --%>
                <c:when test="${currentAction == 'market_loans'}">
                    <div class="data-card">
                        <h4>🌐 Chợ Gọi Vốn Toàn Sàn (Ẩn danh bảo mật)</h4>
                        <table class="table-loan">
                            <thead>
                                <tr>
                                    <th>MÃ ĐƠN VAY</th>
                                    <th>NGƯỜI VAY (ẨN DANH)</th>
                                    <th>LÃI SUẤT</th>
                                    <th>SỐ TIỀN VAY</th>
                                    <th>KỲ HẠN</th>
                                    <th>TIẾN ĐỘ GỌI VỐN</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty marketLoansList}">
                                        <c:forEach var="loan" items="${marketLoansList}">
                                            <tr>
                                                <td>#<strong><c:out value="${loan.applicationId}"/></strong></td>
                                                <td><span style="color:#475569; font-weight: 500;"><c:out value="${loan.maskedBorrowerName}"/></span></td>
                                                <td><span style="color:#3b82f6; font-weight: bold;"><c:out value="${loan.interestRate}"/>% / năm</span></td>
                                                <td><strong><fmt:formatNumber value="${loan.amountRequested}" type="number" groupingUsed="true"/> đ</strong></td>
                                                <td><c:out value="${loan.termMonths}"/> Tháng</td>
                                                <td>
                                                    <c:set var="percent" value="${loan.fundingProgress}"/>
                                                    <div style="width: 80px; background: #e2e8f0; border-radius: 10px; height: 6px; display: inline-block; margin-right: 5px;">
                                                        <div style="width: ${percent}%; background: #22c55e; height: 100%; border-radius: 10px;"></div>
                                                    </div>
                                                    <span style="font-size:12px; font-weight:bold; color:#16a34a;"><fmt:formatNumber value="${percent}" maxFractionDigits="1"/>%</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr><td colspan="6" style="text-align: center; color: #a0aec0; padding: 20px;">Không có đơn gọi vốn nào khác trên sàn.</td></tr>
                                    </c:otherwise>
                                </c:choose>
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
                <h5><c:out value="${n.title}"/></h5>
                <p><c:out value="${n.message}"/></p>
                <small><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </div>
        </c:forEach>
        <c:if test="${empty notifications}"><p style="color:#94a3b8;font-size:13px;">Chưa có thông báo.</p></c:if>
    </aside>

    <button id="backToTop" title="Lên đầu trang">↑</button>
    <script src="${pageContext.request.contextPath}/js/back-to-top.js"></script>

    <script>
        window.addEventListener('DOMContentLoaded', () => {
            const urlParams = new URLSearchParams(window.location.search);
            const actionParam = urlParams.get('action');
            if (actionParam === 're_ekyc') {
                const activeMenu = document.querySelector('.sidebar-menu li.active');
                if (activeMenu) activeMenu.classList.remove('active');
            }

            // Tự động gán ngày lớn nhất cho trường nhập Ngày cấp CIC là hôm nay
            const cicDateInput = document.getElementById('ngayCapCic');
            if (cicDateInput) {
                const today = new Date().toISOString().split('T')[0];
                cicDateInput.setAttribute('max', today);
            }
        });

        function previewImage(input, previewId) {
            const previewBox = document.getElementById(previewId);
            const imgTag = previewBox.querySelector('img');
            if (input.files && input.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    imgTag.src = e.target.result;
                    previewBox.style.display = 'block'; // Chỉ hiển thị khi đã load được dữ liệu base64
                }
                reader.readAsDataURL(input.files[0]);
            }
        }

        function previewCurrency(value) {
            formatVND(value, document.getElementById('currencyPreview'));
        }

        function previewCurrencyUpdate(value) {
            formatVND(value, document.getElementById('updateCurrencyPreview'));
        }

        function formatVND(value, element) {
            if (!value || isNaN(value)) { element.innerText = ''; return; }
            let formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
            element.innerText = '👉 Quy đổi định dạng: ' + formatter.format(value);
        }

        function validateLoanAmount() {
            const inputSotien = document.getElementById('soTienVay');
            const soTienVay = parseFloat(inputSotien.value);
            const hanMucToiDa = parseFloat(inputSotien.getAttribute('data-max')) || 0;
            if (soTienVay > hanMucToiDa) {
                alert("Số tiền đăng ký vay vượt quá hạn mức cho phép (" + hanMucToiDa.toLocaleString('vi-VN') + " đ)!");
                return false;
            }
            return true;
        }
    </script>
</body>
</html>