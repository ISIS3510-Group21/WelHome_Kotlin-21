package com.team21.myapplication.domain.usecase

import com.team21.myapplication.data.model.HousingPostFull
import com.team21.myapplication.data.repository.HousingPostRepository

/**
 * MVVM (Dominio):
 * - Caso de uso del detalle: una única responsabilidad (leer por id).
 * - Mantiene la lógica de negocio separada de UI/VM.
 */
class GetHousingPostByIdUseCase(
    private val repo: HousingPostRepository
) {
    suspend operator fun invoke(housingId: String): HousingPostFull? {
        return repo.getHousingPostById(housingId)
    }
}