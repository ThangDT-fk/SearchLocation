package com.example.searchmapapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var resultsListView: ListView
    private lateinit var noResultsTextView: TextView
    private lateinit var adapter: CustomAdapter
    private val results = mutableListOf<String>()
    private var debounceJob: Job? = null
    private var query: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        resultsListView = findViewById(R.id.resultsListView)
        noResultsTextView = findViewById(R.id.noResultsTextView)

        // Add text change listener to EditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    s?.let {
                        if (it.isNotEmpty()) {
                            query = it.toString()
                            searchAddresses(query)
                        } else {
                            clearResults()
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        resultsListView.setOnItemClickListener { _, _, position, _ ->
            val address = results[position]
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(address)}"))
            startActivity(intent)
        }

        // Initialize clear button for searchEditText
        val searchInputLayout = findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        searchInputLayout.endIconDrawable = getDrawable(R.drawable.ic_clear)
        searchInputLayout.setEndIconOnClickListener {
            searchEditText.text?.clear()
            clearResults()
        }
    }

    private fun clearResults() {
        results.clear()
        noResultsTextView.visibility = TextView.GONE
        adapter.notifyDataSetChanged()
    }

    interface HEREApiService {
        @GET("geocode")
        suspend fun searchAddresses(
            @Query("q") query: String,
            @Query("apiKey") apiKey: String
        ): HEREApiResponse
    }

    data class HEREApiResponse(
        val items: List<AddressItem>?
    )

    data class AddressItem(
        val address: Address,
        val position: Position
    )

    data class Address(
        val label: String
    )

    data class Position(
        val lat: Double,
        val lng: Double
    )

    private fun searchAddresses(query: String) {
        val apiKey = "cB5SoYn1QvyDI99VxWrvwvwVMol33Aw3bOzx0yLONK8"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://geocode.search.hereapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(HEREApiService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("API_CALL", "Searching addresses for query: $query with API key: $apiKey")
                val response = service.searchAddresses(query, apiKey)
                withContext(Dispatchers.Main) {
                    results.clear()
                    response.items?.let { items ->
                        results.addAll(items.map { it.address.label })
                    }
                    if (results.isEmpty()) {
                        noResultsTextView.visibility = TextView.VISIBLE
                    } else {
                        noResultsTextView.visibility = TextView.GONE
                    }
                    adapter = CustomAdapter(this@MainActivity, results, query)
                    resultsListView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_CALL_ERROR", "Error fetching addresses", e)
                    Toast.makeText(this@MainActivity, "Failed to fetch addresses", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
