package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Deserializer {
    @NotNull
    private Path path;

    private Configuration configuration;

    private Integer[] storedScores;

    Place centralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();



    public Deserializer(@NotNull final Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException("Cannot find file to load!");
        }

        this.path = path;
    }

    /**
     * Returns the first non-empty and non-comment (starts with '#') line from the reader.
     *
     * @param br {@link BufferedReader} to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line
     * @throws InvalidGameException if unexpected end of file
     */
    @Nullable
    private String getFirstNonEmptyLine(@NotNull final BufferedReader br) throws IOException {
        String line;
        while((line = br.readLine()) != null){
            line = line.trim();
            if(!line.isEmpty() && line.charAt(0) != '#')
                return line;
        }
        return null;
    }

    public void parseGame() {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;

            int size;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                try {
                    String[] words = line.split(":");
                    size = Integer.parseInt(words[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new InvalidConfigurationError("Fail to parse board size. Please check format.");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                try {
                    String[] words = line.split(":");
                    numMovesProtection = Integer.parseInt(words[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new InvalidConfigurationError("Fail to parse numMovesProtection. Please check format.");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of columns");
            }

            /**
             *  read central place here
             *  If success, assign to {@link Deserializer#centralPlace}
             *  Hint: You may use {@link Deserializer#parsePlace(String)}
             */
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                try {
                    String[] words = line.split(":");
                    centralPlace = parsePlace(words[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new InvalidConfigurationError("Fail to parse central place. Please check format.");
                }
            }else {
                throw new InvalidGameException("Unexpected EOF when parsing number of columns");
            }

            int numPlayers;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                try {
                    String[] words = line.split(":");
                    numPlayers = Integer.parseInt(words[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new InvalidConfigurationError("Fail to parse board size. Please check format.");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }

            /**
             * create an array of players {@link Player} with length of numPlayers, and name it by the read-in name
             * Also create an array representing scores {@link Deserializer#storedScores} of players with length of numPlayers
             */
            Player[] players = new Player[numPlayers];
            storedScores = new Integer[numPlayers];
            for(int i = 0; i < numPlayers; ++i) {
                line = getFirstNonEmptyLine(reader);
                String[] parts = line.split(";");
                players[i] = new Player(parts[0].split(":")[1].trim(),Color.DEFAULT) {
                    @Override
                    public @NotNull Move nextMove(Game game, Move[] availableMoves) {
                        if(availableMoves.length == 0)
                            return null;
                        return availableMoves[0];
                    }
                };
                storedScores[i] = Integer.parseInt(parts[1].split(":")[1]);
            }

            /**
             * try to initialize a configuration object  with the above read-in variables
             * if fail, throw InvalidConfigurationError exception
             * if success, assign to {@link Deserializer#configuration}
             */
            configuration = new Configuration(size, players, numMovesProtection);

            /**
             * Parse the string of move records into an array of {@link MoveRecord}
             * Assign to {@link Deserializer#moveRecords}
             * You should first implement the following methods:
             * - {@link Deserializer#parseMoveRecord(String)}}
             * - {@link Deserializer#parseMove(String)} ()}
             * - {@link Deserializer#parsePlace(String)} ()}
             */
            while((line=getFirstNonEmptyLine(reader)) != null && !line.equals("END")) {
                moveRecords.add(parseMoveRecord(line));
            }
        } catch (InvalidConfigurationError icge) {
            throw icge;
        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }
    }

    public Configuration getLoadedConfiguration(){
        return configuration;
    }

    public Integer[] getStoredScores(){
        return storedScores;
    }

    public ArrayList<MoveRecord> getMoveRecords(){
        return moveRecords;
    }

    /**
     * Parse the string into a {@link MoveRecord}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveRecordString a string of a move record
     * @return a {@link MoveRecord}
     */
    private MoveRecord parseMoveRecord(String moveRecordString){
        String[] words = moveRecordString.split(":");
        if(words.length != 3)
            throw  new InvalidConfigurationError(String.format("invalid move record \"%s\"", moveRecordString));

        String[] names = words[1].split(";");
        String name = names[0].trim();
        Player[] players = configuration.getPlayers();
        for(int i = 0;i < players.length; ++i) {
            if(players[i].getName().equals(name))
                return new MoveRecord(players[i], parseMove(words[2].trim()));
        }
        throw  new InvalidConfigurationError(String.format("invalid move record, can not find player \"%s\"", moveRecordString));
    }

    /**
     * Parse a string of move to a {@link Move}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveString given string
     * @return {@link Move}
     */
    private Move parseMove(String moveString) {
        String[] placesStr = moveString.split("->");
        if(placesStr.length != 2)
            throw  new InvalidConfigurationError(String.format("invalid move \"%s\"", moveString));

        return new Move(parsePlace(placesStr[0]),parsePlace(placesStr[1]));
    }

    /**
     * Parse a string of move to a {@link Place}
     * Handle InvalidConfigurationError if the parse fails.
     * @param placeString given string
     * @return {@link Place}
     */
    private Place parsePlace(String placeString) {
        int x,y;
        if(placeString.length() < 2 || placeString.charAt(0) != '(')
            throw new InvalidConfigurationError(String.format("invalid place \"%s\"",placeString));

        x = Integer.parseInt(placeString.substring(1,placeString.indexOf(',')));

        String[] words = placeString.split(",");
        if(words.length != 2)
            throw new InvalidConfigurationError(String.format("invalid place \"%s\"",placeString));

        y = Integer.parseInt(words[1].substring(0,words[1].indexOf(')')));

        return new Place(x,y);
    }


}
