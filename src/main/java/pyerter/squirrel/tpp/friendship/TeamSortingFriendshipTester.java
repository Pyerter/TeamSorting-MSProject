package pyerter.squirrel.tpp.friendship;

import pyerter.squirrel.tpp.TeamSortingLogger;
import pyerter.squirrel.tpp.core.TeamSorterResult;
import pyerter.squirrel.tpp.core.TeamSorterSolver;
import pyerter.squirrel.tpp.core.TeamSortingGeneratorInput;
import pyerter.squirrel.tpp.core.TeamSortingInput;
import pyerter.squirrel.tpp.io.CsvReader;
import pyerter.squirrel.tpp.io.CsvResultWriter;

import java.util.Arrays;
import java.util.stream.IntStream;

public class TeamSortingFriendshipTester {

    public static boolean useLPFirst = true;

    public static void main(String[] args) {
        int count;
        try {
            count = Integer.parseInt(args[0]);
        } catch (Exception e) {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            runTest(i > 1 ? 0 : 3);
        }
    }

    public static void runTest(int verb) {
        int memberCount = 36; // 36, 36, 18, 80, 75
        int teamCount = 4; // 4, 4, 3, 5, 5
        int roleCount = 3; // 3, 3, 3, 4, 3
        int preferenceCount = 3; // 3, 3, 3, 3, 2
        // Role requirements for each team
        int roleReqLB = 1; // 1, 2, 1, 2, 2
        int roleReqUB = 3; // 3, 3, 2, 4, 5
        // Minimum team sizes
        int minTeamCountLB = 5; // 5, 7, 3, 7, 7
        int minTeamCountUB = 9; // 9, 9, 5, 16, 15
        // Number of roles members have
        int memberRoleCountLB = 1; // 1, 1, 1, 1, 1
        int memberRoleCountUB = 3; // 3, 2, 2, 3, 3
        //if (memberCount < roleCount * roleReqUB * teamCount) memberCount = roleCount * roleReqUB * teamCount;
        //if (memberCount < minTeamCountUB * teamCount) memberCount = minTeamCountUB * teamCount;
        int numbFriendshipsRange = 6; // 6, 6, 5, 10, 11
        int numbFriendships = 5 + ((int)(Math.random() * numbFriendshipsRange)); // 5, 5, 2, 5, 5
        int friendshipSizeLB = 2; // 2, 2, 2, 2, 2
        int friendshipSizeUB = 3; // 3, 3, 3, 4, 4

        TeamSortingInput input = TeamSortingGeneratorInput.generateInput(memberCount, teamCount, roleCount, preferenceCount,
                roleReqLB, roleReqUB, minTeamCountLB, minTeamCountUB, memberRoleCountLB, memberRoleCountUB,
                numbFriendships, friendshipSizeLB, friendshipSizeUB);
        System.out.printf("Members (%d), Teams (%d), Roles (%d), Preferences (%d)%n", memberCount, teamCount, roleCount, preferenceCount);
        System.out.printf("Role Requirements (%d-%d), Min Team Counts (%d-%d), Member Role Counts (%d-%d)%n",
                roleReqLB, roleReqUB, minTeamCountLB, minTeamCountUB, memberRoleCountLB, memberRoleCountUB);
        if (input == null) {
            System.out.printf("Err: Input object returned as null- ran into some error?%n");
            return;
        }

        if (memberCount != input.numbMembers()) {
            System.out.printf("Note -- member count corrected to %d to ensure a solution is possible.", input.numbMembers());
        }

        System.out.printf("Teams: %s%n", input.toPrintTeams());
        System.out.printf("Roles: %s%n", input.toPrintRoles());
        System.out.printf("Members: %s%n", input.toPrintMemberNames());
        System.out.printf("Member Roles: %s%n", input.toPrintMemberRoles());
        System.out.printf("Member Preferences: %s%n", input.toPrintMemberPreferences());
        System.out.printf("Team Role Requirements: %s%n", input.toPrintTeamRoleRequirements());
        System.out.printf("Minimum Team Counts: %s%n", Arrays.toString(IntStream.rangeClosed(0, teamCount - 1).map(input::getTeamMinimumMembers).toArray()).replace(",", ""));


        System.out.printf("%n----- Solving Team Sorting -----%n%n");

        TeamSorterSolver solver = new TeamSorterSolver(input, false);
        TeamSortingLogger logger;
        TeamSorterResult result;
        TeamSorterResult roundedResult;
        boolean caughtFailure = false;
        String failureMessage = "";
        if (useLPFirst) {
            try {
                solver.setUseHardPreferenceObjectiveFunction(false);
                solver.setIgnoreFriendships(false);
                solver.setRoundFriendships(true);
                logger = new TeamSortingLogger(1);
                result = solver.solve(logger);
                roundedResult = result.getRoundedResult();
            } catch (Exception e) {
                caughtFailure = true;
                failureMessage = e.getMessage();
                solver.setUseHardPreferenceObjectiveFunction(false);
                solver.setIgnoreFriendships(true);
                solver.setRoundFriendships(true);
                logger = new TeamSortingLogger(verb);
                result = solver.solve(logger);
                roundedResult = result.getRoundedResult();
            }
        } else {
            solver.setUseHardPreferenceObjectiveFunction(false);
            solver.setIgnoreFriendships(true);
            solver.setRoundFriendships(true);
            logger = new TeamSortingLogger(verb);
            result = solver.solve(logger);
            roundedResult = result.getRoundedResult();
        }

        boolean usingRounded = roundedResult != null;

        logger.log("Direct Result - - - Assignment Values\n" + result.toPrintAssignments(), 3);
        logger.log("Direct Result - - -\n" + result.toPrintFinalAssignments(), 1);
        if (usingRounded) logger.log("Rounded Result - - -\n" + roundedResult.toPrintFinalAssignments(), 1);
        logger.log("Direct Result - - -\n" + result.toPrintFinalFriendshipAssignments(), 1);
        if (usingRounded) logger.log("Rounded Result - - -\n" + roundedResult.toPrintFinalFriendshipAssignments(), 1);
        logger.log("Direct Result - - -\n" + result.toPrintStats());
        logger.log(result.toPrintFinalPreferences());
        logger.log(result.toPrintFinalFriendshipObjective());
        if (usingRounded) logger.log("Rounded Result - - -\n" + roundedResult.toPrintStats());
        if (usingRounded) logger.log(roundedResult.toPrintFinalPreferences());
        if (usingRounded) logger.log(roundedResult.toPrintFinalFriendshipObjective());
        if (usingRounded) {
            logger.log(String.format("Rounding improvement (friendship objective): %.3f", (roundedResult.getFinalFriendshipObjectiveValue() - result.getFinalFriendshipObjectiveValue())));
            logger.log(String.format("Rounding improvement   (standard objective): %.3f", (roundedResult.getObjectiveValue() - result.getObjectiveValue())));
            logger.log(String.format("Maximum value        (friendship objective): %.3f", (result.getTheoreticalMaxFriendshipObjectiveValue())));
            logger.log(String.format("Maximum value          (standard objective): %.3f", (result.getTheoreticalMaxObjectiveValue())));
        }
        if (caughtFailure) {
            System.out.println("--------- Original attempt failed, retried with rounding algorithm on LP without friendship constraints");
            System.out.printf("Failure Message: %s%n", failureMessage);
        }

        CsvResultWriter.subdirectoryName = "genexperiment";
        CsvResultWriter.writeResultToFile(result, "genexperiment");
        CsvResultWriter.subdirectoryName = "genexperiment_rounded";
        if (usingRounded) CsvResultWriter.writeResultToFile(roundedResult, "genexperiment_rounded");
        CsvResultWriter.subdirectoryName = "genexperiment_input";
        CsvReader.writeProblemInputCsv(input, "genexperiment_input");
    }

}
