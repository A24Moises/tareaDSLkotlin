import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.File

const val API_KEY = "T3AiNIX3Av6CJL09ONVs8cqwRlfHxzQVFdpoHK1D"
const val BASE_URL = "https://api.nasa.gov/planetary/apod"

@Serializable
data class NasaResponse(
    val date: String,
    val explanation: String,
    val title: String,
    val url: String? = null,
    val hdurl: String? = null
)

fun getNasaData(date: String): NasaResponse? {
    val urlString = "$BASE_URL?api_key=$API_KEY&date=$date"
    val url = URL(urlString)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    return try {
        val response = connection.inputStream
            .bufferedReader()
            .use { it.readText() }
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString<NasaResponse>(response)
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
    } finally {
        connection.disconnect()
    }
}

fun generateHtmlContent(data: List<NasaResponse>): String {
    val rows = data.joinToString(separator = "") { response ->
        """
        <tr>
            <td>${response.date}</td>
            <td>${response.title}</td>
            <td>${response.explanation.take(100)}...</td>
            <td><a href="${response.url ?: response.hdurl ?: "#"}">Ver imagen</a></td>
        </tr>
        """
    }

    val imageUrls = data.mapNotNull { it.url ?: it.hdurl }.joinToString(prefix = "[\"", separator = "\", \"", postfix = "\"]")

    return """
    <html>
    <head>
        <title>NASA APOD</title>
        <style>
            body {
                color: white;
                background-color: black;
                text-align: center;
            }
            table {
                margin: 0 auto;
                background-color: white;
                color: black;
            }
            a {
                text-decoration: none;
            }
            img {
                border: 2px solid white;
                border-radius: 10px;
            }
        </style>
        <script>
            function getRandomImage() {
                const images = $imageUrls;
                const randomIndex = Math.floor(Math.random() * images.length);
                return images[randomIndex];
            }
            window.onload = function() {
                document.getElementById('randomImage').src = getRandomImage();
            }
        </script>
    </head>
    <body>
        <h1>Base de datos NASA APOD</h1>
        <br>
        <p>Uno de los sitios web mas populares de la NASA es <a href="https://apod.nasa.gov/apod/astropix.html">Astronomy Picture of the Day</a> . De hecho, este sitio web es uno de los mas populares en todas las agencias federales.</p>
        <br>
        <br>
        <table border="1">
            <tr>
                <th>Fecha</th>
                <th>Avistamiento</th>
                <th>Explicacion</th>
                <th>Link a imagen</th>
            </tr>
            $rows
        </table>
         <br>
        <img id="randomImage" src="" alt="Random NASA APOD Image" style="max-width: 100%; height: auto;">
    </body>
    </html>
    """
}

fun main() {
    val dates = listOf(
        "2024-02-12", "2024-02-11", "2024-02-10", "2024-02-09", "2024-02-08",
        "2024-02-07", "2024-02-06", "2024-02-05", "2024-02-04", "2024-02-03"
    )
    val results = dates.mapNotNull { getNasaData(it) }
    val htmlContent = generateHtmlContent(results)
    File("nasa_data.html").writeText(htmlContent)
    print("HTML file generated: nasa_data.html")
    println(generateHtmlContent(results))
}

