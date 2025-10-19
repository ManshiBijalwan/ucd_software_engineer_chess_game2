package com.chess.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.chess.domain.GameState;
import com.chess.domain.Square;

public class SaveLoadTest {

    @Test
    void testRoundTripInitialPosition() throws Exception {
        GameState st = new GameState();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SaveLoadService.save(st, bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        GameState loaded = SaveLoadService.load(bis);

        // Compare a few key squares and toMove
        assertEquals(st.getToMove(), loaded.getToMove());
        assertNotNull(loaded.getBoard().getPiece(new Square(4,0))); // white king e1
        assertNotNull(loaded.getBoard().getPiece(new Square(4,7))); // black king e8
    }
}