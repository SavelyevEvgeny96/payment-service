package ru.sogaz.site.paymentService.service

class GpbCallbackServiceTest {
//    private val signatureVerifier = mock<SignatureVerifier>()
//    private val paymentDao = mock<PaymentDao>()
//    private val orderDao = mock<OrderDao>()
//    private val paymentOperationHistoryDao = mock<PaymentOperationHistoryDao>()
//    private val callbackPaymentDao = mock<CallbackPaymentDao>()
//    private val gpbCallbackMetricService = mock<GpbCallbackMetricServiceImpl>()
//    private val httpServletRequest = mock<HttpServletRequest>()
//
//    private val paymentDao: PaymentDao,
//    private val orderMapper: OrderMapper,
//    private val subOrderMapper: SubOrderMapper,
//    private val signatureVerifier: SignatureVerifier,
//    private val registerCardMapper: RegisterCardMapper,
//    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
//    private val sendMessageProducer: SendMessageProducer,
//    private val props: RabbitProperties,
//    private val gpbCallbackMetricService: GpbCallbackMetricServiceImpl,
//    private val service =
//        GpbCallbackServiceImpl(
//            paymentDao,
//            signatureVerifier,
//            callbackPaymentDao,
//            gpbCallbackMetricService,
//        )
//
//    private val testRequest =
//        GpbCallbackRequest(
//            trxId = "ZLZA2BRR45VP6YF0",
//            merchId = "GCS_merchant2",
//            resultCode = 1,
//            amount = "1004",
//            accountId = "4EEA096E50948B54C32C32455AD6BCAC",
//            orderId = "ord-da03ea907e5b",
//            rrn = "240524141650",
//            authCode = "410545",
//            srcType = "CARD",
//            maskedPan = "444499xxxxxx6000",
//            isFullyAuthenticated = "Y",
//            transmissionDateTime = "1202104556",
//            discountType = "CREDIT",
//            discountAmount = "10000",
//            paymentSystem = "VISA",
//            issuerName = "Sber",
//            ts = "20240524 14:16:50",
//            signature =
//                "Q6WBwrZr%2BW%2BcBlZ1pBgdRcOgr2aAh8cognmOjK7iqmcl5VWIQb0x%2Br8M9COnvaNsQlbuWkc62e2EdxfHqr" +
//                    "6SLcLduOxPQhCan6qKDkAMUuPZYbS1ycISo",
//        )
//
//    @Test
//    fun `processCallback should return success response when all steps are successful`() {
//        val order =
//            Order().apply {
//                id = UUID.randomUUID()
//            }
//
//        val payment = createTestPayment()
//        val orderId = payment.order.id
//        if (orderId != null) {
//            `when`(signatureVerifier.verifySignature(any(), any())).thenReturn(true)
//            `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
//            `when`(orderDao.findById(orderId)).thenReturn(order)
//        }
//        val response = service.processCallback(testRequest, httpServletRequest)
//
//        assertThat(response.body).contains("<code>1</code>")
//        verify(paymentDao).save(payment)
//    }
//
//    @Test
//    fun `processCallback should fail when orderId is Null`() {
//        val payment =
//            createTestPayment()
//
//        `when`(signatureVerifier.verifySignature(testRequest, httpServletRequest)).thenReturn(true)
//        `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
//
//        val response = service.processCallback(testRequest, httpServletRequest)
//
//        assertThat(response.body).contains("<code>2</code>")
//    }
//
//    @Test
//    fun `processCallback should fail when orderId not Found`() {
//        val payment =
//            createTestPayment()
//
//        val orderId = payment.order.id
//        if (orderId != null) {
//            `when`(signatureVerifier.verifySignature(testRequest, httpServletRequest)).thenReturn(true)
//            `when`(paymentDao.findByPaymentBankId(testRequest.trxId)).thenReturn(payment)
//            `when`(orderDao.findById(orderId)).thenReturn(null) // правильно
//        }
//        val response = service.processCallback(testRequest, httpServletRequest)
//
//        assertThat(response.body).contains("<code>2</code>")
//    }
//
//    private fun createTestPayment(
//        bank: BankEnum = BankEnum.GPB,
//        type: PaymentTypeEnum = PaymentTypeEnum.CARD,
//        depersonalization: Boolean = false,
//    ): Payment {
//        val order =
//            Order().apply {
//                id = UUID.randomUUID()
//                premiumAmount = BigDecimal("1000.00").toString()
//                saveCard = false
//            }
//
//        return Payment(
//            id = UUID.randomUUID(),
//            order = order,
//            bank = bank,
//            type = type,
//            depersonalization = depersonalization,
//            urlToReturn =
//                UrlToReturn(
//                    urlToReturnS = null,
//                    urlToReturnF = null,
//                ),
//        )
//    }
}
