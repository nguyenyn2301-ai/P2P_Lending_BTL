<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hợp đồng P2P - ${contractCode}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/contract.css">
</head>
<body class="contract-page">
    <div class="contract-nav no-print">
        <a href="javascript:history.back()">← Quay lại</a>
        <button onclick="window.print()">🖨 In / PDF</button>
    </div>

    <div class="contract-container">
        <div class="contract-header">
            <h1>HỢP ĐỒNG CHO VAY NGANG HÀNG (P2P)</h1>
            <p>Số: <strong>${contractCode}</strong></p>
            <p>Ngày lập: ${disburseDate}</p>
        </div>

        <div class="contract-body">
            <p>Căn cứ quy chế hoạt động Sàn P2P Lending;</p>
            <p>Hôm nay, các bên gồm:</p>
            <p><strong>BÊN A (BÊN CHO VAY):</strong> <span class="highlight">${investorName}</span></p>
            <p><strong>BÊN B (BÊN VAY):</strong> <span class="highlight">${borrowerName}</span></p>
            <p>Thống nhất ký kết hợp đồng với các điều khoản sau:</p>
            <p><strong>Điều 1.</strong> Bên A cho Bên B vay số tiền:
                <span class="highlight"><fmt:formatNumber value="${loan.totalAmount}" groupingUsed="true"/> VNĐ</span>
                (Bằng chữ: <span class="highlight">${amountWords}</span>).</p>
            <p><strong>Điều 2.</strong> Lãi suất trong hạn: <span class="highlight">${loan.interestRate}%/năm</span>.
                Thời hạn: <span class="highlight">${loan.termMonths} tháng</span>.</p>
            <p><strong>Điều 3.</strong> Phí dịch vụ sàn (10%): <fmt:formatNumber value="${loan.serviceFee}" groupingUsed="true"/> VNĐ.
                Số tiền giải ngân thực tế: <fmt:formatNumber value="${loan.actualDisbursed}" groupingUsed="true"/> VNĐ.</p>
            <p><strong>Điều 4.</strong> Các bên cam kết thực hiện đúng nghĩa vụ thanh toán theo lịch trên hệ thống điện tử.</p>
        </div>

        <div class="signature-row">
            <div class="digital-signature">
                <h4>BÊN A (Cho vay)</h4>
                <p class="signed">✓ ĐÃ KÝ SỐ ĐIỆN TỬ - Chứng thư số phát hành bởi tổ chức CA</p>
                <p>${investorName}</p>
            </div>
            <div class="digital-signature">
                <h4>BÊN B (Vay)</h4>
                <p class="signed">✓ ĐÃ KÝ SỐ ĐIỆN TỬ - Chứng thư số phát hành bởi tổ chức CA</p>
                <p>${borrowerName}</p>
            </div>
        </div>
    </div>
</body>
</html>
