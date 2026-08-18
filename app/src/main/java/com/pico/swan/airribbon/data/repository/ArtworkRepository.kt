package com.pico.swan.airribbon.data.repository

import com.pico.swan.airribbon.domain.model.ArtworkDocument
import com.pico.swan.airribbon.domain.model.SaveResult

interface ArtworkRepository {
    fun load(): ArtworkDocument?
    fun save(document: ArtworkDocument): SaveResult
    fun clear(): SaveResult
}

class InMemoryArtworkRepository(initial: ArtworkDocument? = null) : ArtworkRepository {
    var document: ArtworkDocument? = initial
        private set

    override fun load(): ArtworkDocument? = document

    override fun save(document: ArtworkDocument): SaveResult {
        this.document = document
        return SaveResult(true, "已保存到本机")
    }

    override fun clear(): SaveResult {
        document = null
        return SaveResult(true, "本地作品已清空")
    }
}
