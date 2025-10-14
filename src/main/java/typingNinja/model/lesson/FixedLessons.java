package typingNinja.model.lesson;

public class FixedLessons {
    public static String passageFor(String lessonType) {
        switch (lessonType) {
            case "1a": return "f j f j fj jf ff jj fjf jfj fjjf jfjj fff jjj f j f j fj jf fjf jfj";
            case "1b": return "g h g h gh hg gg hh ghg hgh ggh hgg ggg hhh gh hg ghg hgh";
            case "1c": return "d k d k dk kd dd kk dkd kdk dkk kdd ddd kkk dk kd dkk kdk";
            case "1d": return "s l s l sl ls ss ll sls lsl sll lss sss lll sl ls sls lsl";
            case "1e": return "a ; a ; a; ;a aa ;; a;a ;a; a;; ;;a aaa ;;; a; a; ;a a;";
            case "1f": return "a s d f g h j k l ; as df gh jk l; asdf jkl; had salad ask a lad as; la;";
            case "2a": return "r t y u rty try yurt try rut tuty tury yurt yurt try try rty uty yut";
            case "2b": return "q w e i o p qwe wew ewe woe weep pip pop pie poi io qwi wiop pew pew";
            case "2c": return "qwerty yuiop qwertyuiop qwerty yuiop qwertyuiop qwe rty uiop qwerty";
            case "2d": return "c v b n cv bn nv vb cvc bnb nvn vbn bnv nvb cnn vbv nnn bbb ccc vvv";
            case "2e": return "z x m zx xz mz zm zz xx mm zxm mxz zzz xxx mmm zx mz xz zm";
            case "2f": return "z x c v b n m zxcv bnm zxcvbnm mnbvcxz zxc bnm zxcvbnm zxcv bnm";
            case "3a": return "4 5 6 7 45 56 67 76 65 54 4567 7654 46 57 64 75 456 567";
            case "3b": return "1 2 3 8 9 0 12 23 89 90 321 098 123 890 19 28 30 201 908";
            case "3c": return "1 2 3 4 5 6 7 8 9 0 1234567890 0987654321 13579 24680 1122334455";
            case "3d": return "A S D F G H J K L : AS DF GH JK L: ASDF JKL: HAD SALAD ASK A LAD";
            case "3e": return "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG PACK MY BOX WITH FIVE DOZEN LIQUOR JUGS";
            case "3f": return "! ? , . ; : - _ ( ) [ ] { } + = / \\ @ # $ % & * ^ ~";
            case "4a": return "Practice smooth rhythm with simple words and short phrases. Keep hands relaxed and eyes on the screen.";
            case "4b": return "Type longer lines with commas, periods, and steady spacing. Build speed gradually without forcing errors.";
            case "4c": return "Mix letters and numbers: room 204, gate 17, and 3 quick notes. Maintain accuracy while increasing pace.";
            case "4d": return "Add punctuation and numbers: plan A-3, section 4B, total 1,296 units; verify, adjust, and continue.";
            case "4e": return "Challenging mix: swap cases, weave symbols, and break patterns - fast yet precise. Target 60+ WPM with 95% accuracy.";
            case "4f": return "Expert drill: {caps, nums, symbols} -> A7b9, zX-4_5, 3/8 + 5*2 = 13; verify: [OK] (pass) @ 100%.";
            default:   return "Default lesson text";
        }
    }
}
