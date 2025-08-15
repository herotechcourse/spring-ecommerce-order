package ecommerce.controller.admin.usecase

import ecommerce.dto.ActiveMemberDTO

interface FindMembersWithRecentCartActivityUseCase {
    fun findMembers(): List<ActiveMemberDTO>
}
