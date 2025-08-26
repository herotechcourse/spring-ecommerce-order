package ecommerce.exception

class MemberEmailAlreadyExistsException(override val message: String = "Member email already exists.") : RuntimeException(message)
