package typingNinja.model.lesson;

/**
 * Holds static lesson passages used by the legacy curriculum.
 */
public class FixedLessons {
    /**
     * Looks up the practice passage associated with the supplied lesson id.
     *
     * @param lessonType short lesson identifier (e.g. {@code "1a"})
     * @return the matching passage or a default placeholder
     */
    public static String passageFor(String lessonType) {
        // Map static lesson ids to curated practice passages.
        switch (lessonType) {
            case "1a": return (
                "fj jf ff jj fjf jfj jfjj fjjf fff jjj fj jf ff jj fjf jfj jfjj fjjf " +
                "f j fj jf ff jj fjf jfj ffj jff fjj jfj ff jj fj jf fjf jfj jfjj fjjf " +
                "fj jf ff jj fjf jfj jfjj fjjf fff jjj fj jf ff jj fjf jfj jfjj fjjf " +
                "f j fj jf ff jj fjf jfj ffj jff fjj jfj ff jj fj jf fjf jfj jfjj fjjf " +
                "fj jf ff jj fjf jfj jfjj fjjf fff jjj fj jf ff jj fjf jfj jfjj fjjf " +
                "f j fj jf ff jj fjf jfj ffj jff fjj jfj ff jj fj jf fjf jfj jfjj fjjf"
            );
            case "1b": return (
                "gh hg gg hh ghg hgh ggh hgg ggg hhh gh hg ghg hgh gh hg gg hh ghg hgh " +
                "ggh hgg ghg hgh ggh hgg ggg hhh gh hg ghg hgh gh hg gg hh ghg hgh ggh hgg " +
                "gh hg gg hh ghg hgh ggh hgg ggg hhh gh hg ghg hgh gh hg gg hh ghg hgh " +
                "ggh hgg ghg hgh ggh hgg ggg hhh gh hg ghg hgh gh hg gg hh ghg hgh ggh hgg"
            );
            case "1c": return (
                "dk kd dd kk dkd kdk dkk kdd ddd kkk dk kd dkk kdk dk kd dd kk dkd kdk " +
                "dkk kdd ddd kkk dk kd dkk kdk dk kd dd kk dkd kdk dkk kdd ddd kkk dk kd " +
                "dk kd dd kk dkd kdk dkk kdd ddd kkk dk kd dkk kdk dk kd dd kk dkd kdk " +
                "dkk kdd ddd kkk dk kd dkk kdk dk kd dd kk dkd kdk dkk kdd ddd kkk dk kd"
            );
            case "1d": return (
                "sl ls ss ll sls lsl sll lss sss lll sl ls sls lsl sl ls ss ll sls lsl " +
                "sll lss sss lll sl ls sls lsl sl ls ss ll sls lsl sll lss sss lll sl ls " +
                "sl ls ss ll sls lsl sll lss sss lll sl ls sls lsl sl ls ss ll sls lsl " +
                "sll lss sss lll sl ls sls lsl sl ls ss ll sls lsl sll lss sss lll sl ls"
            );
            case "1e": return (
                "a; ;a aa ;; a;a ;a; a;; ;;a aaa ;;; aa; a;a a; ;a a; a; ;a aa ;; a;a ;a; " +
                ";;a a;; a;a aa; ;a; a;a a; ;a ;; a;a ;a; a;; ;;a aa a; ;a a; aa ;; a;a " +
                "a; ;a aa ;; a;a ;a; a;; ;;a aaa ;;; aa; a;a a; ;a a; a; ;a aa ;; a;a ;a; " +
                ";;a a;; a;a aa; ;a; a;a a; ;a ;; a;a ;a; a;; ;;a aa a; ;a a; aa ;; a;a"
            );
            case "1f": return (
                "asdf jkl; asdf jkl; sal asd fall hall; ask lass; flag; all glass; sad lad; " +
                "as asd asdf jkl; asdf jkl; fald; sall; gala; laska; has jad; " +
                "asdf jkl; asdf jkl; dash flask; all ask; shall ask; fall salad; asdf jkl; " +
                "asdf jkl; asdf jkl; sal asd fall hall; ask lass; flag; all glass; sad lad; asdf jkl;"
            );
            case "2a": return (
                "rty ury try try rty uty yut yurt rutt try utu yrty urut trut yurt rtyu " +
                "try yurt tury rty ury try rty uty yut ruty yurt try rty uty yurt try " +
                "rty ury try try rty uty yut yurt rutt try utu yrty urut trut yurt rtyu"
            );
            case "2b": return (
                "qwe wew ewe woe weep pip pop pie poi qwi wiop pew pew qwe pei wop pew woi " +
                "qwe wew ewe woe weep pip pop pie poi qwi wiop pew pew qwe pei wop pew woi " +
                "qwe wew ewe woe weep pip pop pie poi qwi wiop pew pew qwe pei wop pew woi"
            );
            case "2c": return (
                "qwertyuiop qwerty yuiop qwertyuiop qwe rty uiop qwerty qwertyuiop uiop qwerty " +
                "qwertyuiop qwerty yuiop qwertyuiop qwe rty uiop qwerty qwertyuiop uiop qwerty " +
                "qwertyuiop qwerty yuiop qwertyuiop qwe rty uiop qwerty qwertyuiop uiop qwerty"
            );
            case "2d": return (
                "cvbn bnv nvb vbn cvc bnb nvn vbn bnv nvb cvbn bnv nvb vbn cvc bnb nvn vbn " +
                "bnv nvb cvbn bnv nvb vbn cvc bnb nvn vbn bnv nvb cvbn bnv nvb vbn cvc bnb " +
                "cvbn bnv nvb vbn cvc bnb nvn vbn bnv nvb cvbn bnv nvb vbn cvc bnb nvn vbn"
            );
            case "2e": return (
                "zx xz mz zm zz xx mm zxm mxz xzm mzz xxm zxm mxz zx mz xz zm xzm mzx zzx " +
                "zx xz mz zm zz xx mm zxm mxz xzm mzz xxm zxm mxz zx mz xz zm xzm mzx zzx " +
                "zx xz mz zm zz xx mm zxm mxz xzm mzz xxm zxm mxz zx mz xz zm xzm mzx zzx"
            );
            case "2f": return (
                "zxcvbnm mnbvcxz zxcv bnm zxc vb nm zx cv bn m zxcvbnm mnbvcxz zxcvbnm mnbvcxz " +
                "zxcvbnm mnbvcxz zxcv bnm zxc vb nm zx cv bn m zxcvbnm mnbvcxz zxcvbnm mnbvcxz " +
                "zxcvbnm mnbvcxz zxcv bnm zxc vb nm zx cv bn m zxcvbnm mnbvcxz zxcvbnm mnbvcxz"
            );
            case "3a": return (
                "45 56 67 76 65 54 4567 7654 46 57 64 75 45 56 67 76 65 54 4567 7654 46 57 64 75 " +
                "45 56 67 76 65 54 4567 7654 46 57 64 75 45 56 67 76 65 54 4567 7654 46 57 64 75 " +
                "45 56 67 76 65 54 4567 7654 46 57 64 75"
            );
            case "3b": return (
                "12 23 89 90 321 098 123 890 19 28 30 201 908 123 890 12 23 89 90 321 098 201 908 " +
                "12 23 89 90 321 098 123 890 19 28 30 201 908 123 890 12 23 89 90 321 098 201 908 " +
                "12 23 89 90 321 098 123 890 19 28 30 201 908"
            );
            case "3c": return (
                "1234567890 0987654321 13579 24680 1122334455 0011223344 5566778899 1234567890 13579 24680 " +
                "1234567890 0987654321 13579 24680 1122334455 0011223344 5566778899 1234567890 13579 24680 " +
                "1234567890 0987654321 13579 24680"
            );
            case "3d": return (
                "ASDF JKL: AS DF GH JK L: ASDF JKL: ASDF JKL: LA LA: AS DF JK: ASDF JKL: " +
                "ASDF JKL: AS DF GH JK L: ASDF JKL: L: A: S: D: F: G: H: J: K: L: ASDF JKL: " +
                "AS DF GH JK L: ASDF JKL: AS DF JK: ASDF JKL:"
            );
            case "3e": return (
                "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG PACK MY BOX WITH FIVE DOZEN LIQUOR JUGS " +
                "BRIGHT VIXENS JUMP DOZY FOWL QUICKLY WALTZ NYMPH FOR QUICK JIGS VEX BOXER PACKS WITH PRIDE " +
                "SIX QUICK BRICK JUGS MOVE HEAVY BOX FOR THE LAZY DWARF " +
                "WIZARDS JINX MY QUAINT DOVE WITH FIVE ZIPPED BAGS"
            );
            case "3f": return (
                "! ? , . ; : - _ ( ) [ ] { } + = / \\ @ # $ % & * ^ ~ " +
                "( [ { } ] ) - _ : ; , . ! ? + = / \\ @ # $ % & * ^ ~ : ; - _ ( ) [ ] { } " +
                "@ # $ % ^ & * ( ) - _ + = [ ] { } / \\ : ; , . ! ?"
            );
            case "4a": return (
                "Practice even rhythm with short phrases and minimal pauses. Keep hands relaxed, eyes up, and hold a steady pace " +
                "throughout the paragraph. Maintain clean spacing and avoid corrections unless essential to keep flow steady."
            );
            case "4b": return (
                "Type longer lines with commas, periods, and careful spacing. Build speed gradually without forcing errors; " +
                "focus on consistent accuracy across full sentences, then increase tempo once stability is maintained."
            );
            case "4c": return (
                "Mix letters and numbers naturally: gate 17 closes at 21:30, row 204 seats 12 to 19, and bay 5 stores 3 boxes. " +
                "Keep a smooth cadence as you switch between words and digits while minimizing backspaces."
            );
            case "4d": return (
                "Add punctuation and numbers: plan A-3, section 4B, total 1,296 units; verify, adjust, and continue. Keep lines " +
                "readable, avoid repeated stumbles, and aim for stable speed under pressure."
            );
            case "4e": return (
                "Challenging mix: swap CASES mid-line, weave symbols like #, @, %, &, and break patterns with hyphens and colons. " +
                "Aim for precision first, then push speed while holding above 95 percent accuracy."
            );
            case "4f": return (
                "Expert drill: {caps, nums, symbols} -> A7b9, zX-4_5, 3/8 + 5*2 = 13; verify: [OK] (pass) @ 100%. Then repeat with " +
                "variants: C4v2, nM-7+8, 42 / 6 = 7; confirm totals, bracket the result, and finish cleanly."
            );
            default:   return "Default lesson text";
        }
    }
}
