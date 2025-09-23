package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID>
