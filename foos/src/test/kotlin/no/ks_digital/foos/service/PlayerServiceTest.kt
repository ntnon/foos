package no.ks_digital.foos.service

import no.ks_digital.foos.entity.Player
import no.ks_digital.foos.repository.PlayerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*

@ExtendWith(MockitoExtension::class)
class PlayerServiceTest {

    @Mock
    private lateinit var playerRepository: PlayerRepository

    @InjectMocks
    private lateinit var playerService: PlayerService

    // --- getAllPlayers ---

    @Test
    fun `getAllPlayers returns sorted list of players`() {
        val players = listOf(Player(1L, "Alice"), Player(2L, "Bob"))
        `when`(playerRepository.findAll(Sort.by("name"))).thenReturn(players)

        val result = playerService.getAllPlayers()

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Bob", result[1].name)
    }

    @Test
    fun `getAllPlayers returns empty list when no players exist`() {
        `when`(playerRepository.findAll(Sort.by("name"))).thenReturn(emptyList())

        val result = playerService.getAllPlayers()

        assertTrue(result.isEmpty())
    }

    // --- getPlayerById ---

    @Test
    fun `getPlayerById returns player when found`() {
        val player = Player(1L, "Alice")
        `when`(playerRepository.findById(1L)).thenReturn(Optional.of(player))

        val result = playerService.getPlayerById(1L)

        assertTrue(result.isPresent)
        assertEquals("Alice", result.get().name)
    }

    @Test
    fun `getPlayerById returns empty Optional when player not found`() {
        `when`(playerRepository.findById(99L)).thenReturn(Optional.empty())

        val result = playerService.getPlayerById(99L)

        assertFalse(result.isPresent)
    }

    // --- getPlayerByName ---

    @Test
    fun `getPlayerByName returns player when found`() {
        val player = Player(1L, "Alice")
        `when`(playerRepository.findByName("Alice")).thenReturn(Optional.of(player))

        val result = playerService.getPlayerByName("Alice")

        assertTrue(result.isPresent)
        assertEquals(1L, result.get().playerId)
    }

    @Test
    fun `getPlayerByName returns empty Optional when name not found`() {
        `when`(playerRepository.findByName("Ghost")).thenReturn(Optional.empty())

        val result = playerService.getPlayerByName("Ghost")

        assertFalse(result.isPresent)
    }

    // --- createPlayer ---

    @Test
    fun `createPlayer saves and returns new player`() {
        val saved = Player(1L, "Alice")
        `when`(playerRepository.existsByName("Alice")).thenReturn(false)
        `when`(playerRepository.save(any())).thenReturn(saved)

        val result = playerService.createPlayer("Alice")

        assertEquals("Alice", result.name)
        verify(playerRepository).save(any())
    }

    @Test
    fun `createPlayer throws IllegalArgumentException when name already exists`() {
        `when`(playerRepository.existsByName("Alice")).thenReturn(true)

        val ex = assertThrows<IllegalArgumentException> {
            playerService.createPlayer("Alice")
        }

        assertTrue(ex.message!!.contains("Alice"))
        verify(playerRepository, never()).save(any())
    }

    // --- updatePlayer ---

    @Test
    fun `updatePlayer saves player with new name`() {
        val existing = Player(1L, "Alice")
        val updated = Player(1L, "Alicia")
        `when`(playerRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(playerRepository.existsByName("Alicia")).thenReturn(false)
        `when`(playerRepository.save(any())).thenReturn(updated)

        val result = playerService.updatePlayer(1L, "Alicia")

        assertEquals("Alicia", result.name)
        verify(playerRepository).save(any())
    }

    @Test
    fun `updatePlayer throws NoSuchElementException when player not found`() {
        `when`(playerRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            playerService.updatePlayer(99L, "NewName")
        }

        verify(playerRepository, never()).save(any())
    }

    @Test
    fun `updatePlayer throws IllegalArgumentException when new name is taken by another player`() {
        val existing = Player(1L, "Alice")
        `when`(playerRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(playerRepository.existsByName("Bob")).thenReturn(true)

        assertThrows<IllegalArgumentException> {
            playerService.updatePlayer(1L, "Bob")
        }

        verify(playerRepository, never()).save(any())
    }

    @Test
    fun `updatePlayer allows saving with the same name as before`() {
        val existing = Player(1L, "Alice")
        `when`(playerRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(playerRepository.existsByName("Alice")).thenReturn(true)
        `when`(playerRepository.save(any())).thenReturn(existing)

        // Should not throw even though name "exists", because it belongs to the same player
        val result = playerService.updatePlayer(1L, "Alice")

        assertEquals("Alice", result.name)
        verify(playerRepository).save(any())
    }

    // --- deletePlayer ---

    @Test
    fun `deletePlayer calls deleteById when player exists`() {
        `when`(playerRepository.existsById(1L)).thenReturn(true)

        playerService.deletePlayer(1L)

        verify(playerRepository).deleteById(1L)
    }

    @Test
    fun `deletePlayer throws NoSuchElementException when player does not exist`() {
        `when`(playerRepository.existsById(99L)).thenReturn(false)

        assertThrows<NoSuchElementException> {
            playerService.deletePlayer(99L)
        }

        verify(playerRepository, never()).deleteById(any())
    }

    // --- getPlayers (paginated) ---

    @Test
    fun `getPlayers without search returns all players paged`() {
        val pageable = PageRequest.of(0, 20, Sort.by("name"))
        val page = PageImpl(listOf(Player(1L, "Alice")), pageable, 1)
        `when`(playerRepository.findAll(pageable)).thenReturn(page)

        val result = playerService.getPlayers(null, 0, 20)

        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getPlayers with search filters by name`() {
        val pageable = PageRequest.of(0, 20, Sort.by("name"))
        val page = PageImpl(listOf(Player(1L, "Alice")), pageable, 1)
        `when`(playerRepository.findByNameContainingIgnoreCase("ali", pageable)).thenReturn(page)

        val result = playerService.getPlayers("ali", 0, 20)

        assertEquals(1, result.totalElements)
        assertEquals("Alice", result.content[0].name)
    }
}
