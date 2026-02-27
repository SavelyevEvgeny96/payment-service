package ru.sogaz.site.paymentService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentRepository
import java.sql.ResultSet
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
class PaymentDaoImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val paymentRepository: PaymentRepository,
) : PaymentDao {
    companion object {
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Платеж не найден"
        const val LOG_ERROR_GET_PAYMENT_SAVE = "Не удалось сохранить данные платежа"
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Ошибка поиска платежа по order_id Exception:  "
        const val PAYMENT_NOT_FOUND = "Ошибка запроса смены статуса. Указанный ордер не найден"
        const val LOG_ERROR_PAYMENT_FIND = "Не удалось найти платеж по данному bankId"
        const val ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления платежа payment_id == null"
    }

    private val logger = loggerFor(javaClass)

    override fun getPaymentFromBankId(bankId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(bankId)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_GET_PAYMENT_BY_ORDER_ID, e)
            throw InnerException(getTraceId(), "$PAYMENT_NOT_FOUND ${e.message}")
        }

    override fun findByPaymentBankId(paymentId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(paymentId)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_PAYMENT_FIND, e)
            throw InnerException(getTraceId(), "$LOG_ERROR_PAYMENT_FIND ${e.message}")
        }

    override fun findLastPaymentByOrderId(orderId: UUID): Optional<Payment> =
        paymentRepository.findFirstByOrderIdOrderByUpdateDateDesc(orderId)

    override fun findByPaymentOrderId(orderId: UUID?): Optional<Payment> =
        paymentRepository.findAllByOrderIdAndState(orderId, PaymentStatusEnum.SUCCESS)

    override fun save(payment: Payment): Payment =
        try {
            paymentRepository.save(payment)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_GET_PAYMENT_SAVE, e)
            throw InnerException(getTraceId(), "$LOG_ERROR_GET_PAYMENT_SAVE ${e.message}")
        }

    override fun findByStatuses(statuses: List<PaymentStatusEnum>): List<Payment> = paymentRepository.findByStatuses(statuses)

    override fun batchInsertPayment(payments: List<Payment>): List<UUID> {
        if (payments.isEmpty()) return emptyList()

        val tuple = "(" + List(6) { "?" }.joinToString(", ") + ", 'NEW')"
        val valuesSql = payments.joinToString(",") { tuple }

        val sql =
            """
            INSERT INTO payments (
                order_id,
                create_date,
                update_date,
                bank,
                type,
                key_card,
                state
            VALUES $valuesSql
            RETURNING id
            """.trimIndent()

        val args = ArrayList<Any?>(payments.size * 14)
        payments.forEach { p ->
            args += p.order
            args += p.createDate?.let { Instant.from(it) } // create_date
            args += p.updateDate.let { Instant.from(it) } // update_date
            args += p.bank // bank
            args += p.type
            args += p.keyCard
        }
        val mapper =
            RowMapper { rs: ResultSet, _: Int ->
                rs.getObject("id", UUID::class.java)
            }
        return jdbcTemplate.query(sql, mapper, *args.toTypedArray())
    }
}
