package dmi.developments.skin_disease_cam.data.repository

import dmi.developments.skin_disease_cam.data.dao.ResultDao
import dmi.developments.skin_disease_cam.data.entity.Result
import javax.inject.Inject

class ResultRepository @Inject constructor(
    private val dao: ResultDao
) {

    fun getAllResults() = dao.getAll()

    suspend fun insertResult(result: Result) {
        dao.insert(result)
    }

    suspend fun deleteResult(result: Result) {
        dao.delete(result)
    }
}
