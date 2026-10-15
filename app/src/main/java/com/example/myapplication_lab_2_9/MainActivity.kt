package com.example.myapplication_lab_2_9

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication_lab_2_9.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class MainActivity : AppCompatActivity() {

    private val tag = "CoroutinesPractice"
    private lateinit var binding: ActivityMainBinding
    private var calculationJob: Job? = null

    private val ceh = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "Глобальне перехоплення крашу: ${throwable.message}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBlock1()
        setupBlock3()
        setupBlock4()
    }

    private fun setupBlock1() {
        binding.btnStart.setOnClickListener {
            calculationJob?.cancel()

            calculationJob = lifecycleScope.launch(Dispatchers.Default) {
                try {
                    for (i in 1..100) {
                        Thread.sleep(50)
                        ensureActive()
                        Log.d(tag, "Обробка: $i%")
                    }
                    Log.d(tag, "Роботу успішно завершено!")
                } catch (e: CancellationException) {
                    Log.d(tag, "Обробку скасовано користувачем!")
                    throw e
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            calculationJob?.cancel()
        }
    }

    private fun setupBlock3() {
        binding.btnTestCrash.setOnClickListener {
            lifecycleScope.launch {
                supervisorScope {
                    launch {
                        delay(1000)
                        try {
                            throw RuntimeException("Банер не завантажився!")
                        } catch (e: Exception) {
                            Log.e(tag, "Помилка завантаження банера: ${e.message}")
                        }
                    }

                    launch {
                        delay(2000)
                        Log.d(tag, "Стрічка новин завантажена")
                    }
                }
            }
        }
    }

    private fun setupBlock4() {
        binding.btnGlobalCrash.setOnClickListener {
            val safeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + ceh)

            safeScope.launch {
                delay(500)
                throw IllegalArgumentException("Ой!")
            }
        }
    }
}