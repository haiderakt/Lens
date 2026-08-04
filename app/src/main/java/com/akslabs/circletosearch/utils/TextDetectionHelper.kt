package com.akslabs.circletosearch.utils

import android.graphics.Bitmap
import com.akslabs.circletosearch.ui.components.TextNode
import com.akslabs.circletosearch.ui.components.Word
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

object TextDetectionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detectText(bitmap: Bitmap): List<TextNode> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        
        return result.textBlocks.map { block ->
            TextNode(
                id = UUID.randomUUID().toString(),
                fullText = block.text,
                bounds = block.boundingBox ?: android.graphics.Rect(),
                words = block.lines.flatMap { line -> 
                    line.elements.map { element ->
                        Word(
                            text = element.text,
                            index = 0,
                            startIndex = 0,
                            endIndex = element.text.length,
                            bounds = android.graphics.RectF(element.boundingBox ?: android.graphics.Rect())
                        )
                    }
                }
            )
        }
    }
}
