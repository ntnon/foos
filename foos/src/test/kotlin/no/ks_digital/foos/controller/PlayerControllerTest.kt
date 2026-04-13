package no.ks_digital.foos.controller

import tools.jackson.databind.ObjectMapper
import no.ks_digital.foos.entity.Player
import no.ks_digital.foos.service.PlayerService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(PlayerController::class)
@ActiveProfiles("test")
class PlayerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var playerService: PlayerService

    private fun player(id: Long, name: String) = Player(playerId = id, name = name)

    // region GET /api/players

    @Test
    fun `GET api-players returns all players as a list`() {
        val players = listOf(player(1L, "Alice"), player(2L, "Bob"))
        given(playerService.getAllPlayers()).willReturn(players)

        mockMvc.perform(get("/api/players"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[1].name").value("Bob"))
    }

    @Test
    fun `GET api-players with search returns paginated results`() {
        val players = listOf(player(1L, "Alice"))
        val page = PageImpl(players, PageRequest.of(0, 20), 1)
        given(playerService.getPlayers("Ali", 0, 20)).willReturn(page)

        mockMvc.perform(
            get("/api/players")
                .param("search", "Ali")
                .param("paginate", "true")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Alice"))
    }

    // endregion

    // region GET /api/players/{id}

    @Test
    fun `GET api-players by id returns player when found`() {
        given(playerService.getPlayerById(1L)).willReturn(Optional.of(player(1L, "Alice")))

        mockMvc.perform(get("/api/players/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.playerId").value(1))
    }

    @Test
    fun `GET api-players by id returns 404 when player does not exist`() {
        given(playerService.getPlayerById(99L)).willReturn(Optional.empty())

        mockMvc.perform(get("/api/players/99"))
            .andExpect(status().isNotFound)
    }

    // endregion

    // region GET /api/players/name/{name}

    @Test
    fun `GET api-players by name returns player when found`() {
        given(playerService.getPlayerByName("Alice")).willReturn(Optional.of(player(1L, "Alice")))

        mockMvc.perform(get("/api/players/name/Alice"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Alice"))
    }

    @Test
    fun `GET api-players by name returns 404 when player does not exist`() {
        given(playerService.getPlayerByName("Ghost")).willReturn(Optional.empty())

        mockMvc.perform(get("/api/players/name/Ghost"))
            .andExpect(status().isNotFound)
    }

    // endregion

    // region POST /api/players

    @Test
    fun `POST api-players creates player and returns 201`() {
        val created = player(1L, "Alice")
        given(playerService.createPlayer("Alice")).willReturn(created)

        mockMvc.perform(
            post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePlayerRequest("Alice")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.playerId").value(1))
    }

    @Test
    fun `POST api-players returns 400 when name already exists`() {
        given(playerService.createPlayer("Alice")).willThrow(IllegalArgumentException("Player with name 'Alice' already exists"))

        mockMvc.perform(
            post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePlayerRequest("Alice")))
        )
            .andExpect(status().isBadRequest)
    }

    // endregion

    // region DELETE /api/players/{id}

    @Test
    fun `DELETE api-players returns 204 when player is deleted`() {
        mockMvc.perform(delete("/api/players/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE api-players returns 404 when player does not exist`() {
        willThrow(NoSuchElementException("Player not found with id: 99"))
            .given(playerService).deletePlayer(99L)

        mockMvc.perform(delete("/api/players/99"))
            .andExpect(status().isNotFound)
    }

    // endregion
}
