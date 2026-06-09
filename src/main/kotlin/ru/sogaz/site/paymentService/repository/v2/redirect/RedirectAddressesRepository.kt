package ru.sogaz.site.paymentService.repository.v2.redirect

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentService.model.v2.entity.redirect.RedirectAddresses
import java.util.UUID

interface RedirectAddressesRepository : JpaRepository<RedirectAddresses, UUID>
