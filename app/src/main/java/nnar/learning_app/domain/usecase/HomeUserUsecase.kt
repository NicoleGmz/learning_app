package nnar.learning_app.domain.usecase

import nnar.learning_app.data.repository.MobileRepository
import nnar.learning_app.domain.model.Mobile

class HomeUserUsecase(private val mobileRepository: MobileRepository) {

    fun getAllMobiles() = mobileRepository.getAllMobiles()
    fun addMobile(mobile: Mobile) = mobileRepository.addMobile(mobile)
}