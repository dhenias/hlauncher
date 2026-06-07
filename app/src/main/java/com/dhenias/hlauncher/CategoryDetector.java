package com.dhenias.hlauncher;

public class CategoryDetector {

    public static String detect(String name, String pkg) {
        String n = name.toLowerCase();
        String p = pkg.toLowerCase();

        // Social
        if (anyOf(n, p, "whatsapp","telegram","instagram","twitter","facebook","tiktok",
                "snapchat","discord","line","signal","messenger","wechat","linkedin",
                "reddit","tumblr","mastodon","threads","bluesky","skype","zoom","meet")) return "social";

        // Games
        if (anyOf(n, p, "game","play","clash","mobile legends","mlbb","pubg","freefire",
                "genshin","minecraft","roblox","valorant","chess","puzzle","arena","legends",
                "strike","royale","fortnite","arcade","casino","slots","poker","sudoku",
                "brawl","among us","cod","duty","honkai","sekai","limbus","hsr")) return "games";

        // Media
        if (anyOf(n, p, "youtube","spotify","netflix","music","video","player","radio",
                "podcast","soundcloud","deezer","tidal","prime","disney","hulu","viu",
                "wetv","bilibili","twitch","stream","media","gallery","photo","camera",
                "canva","capcut","inshot","kinemaster","vlc","mx player")) return "media";

        // Finance
        if (anyOf(n, p, "bank","pay","wallet","money","transfer","invest","crypto",
                "bitcoin","trading","saham","gopay","ovo","dana","bca","bri","bni",
                "mandiri","jenius","jago","flip","xendit","finance","tax","budget")) return "finance";

        // Shopping
        if (anyOf(n, p, "shopee","tokopedia","lazada","amazon","ebay","shop","store",
                "market","bukalapak","blibli","zalora","traveloka","tiket","booking",
                "agoda","airbnb","grab","gojek","ojek","delivery","food","pesan")) return "shopping";

        // Health
        if (anyOf(n, p, "health","fit","workout","gym","run","walk","step","calorie",
                "diet","medical","doctor","hospital","pharmacy","alodokter","halodoc",
                "sleep","meditate","yoga","strava","nike run","period","cycle")) return "health";

        // Education
        if (anyOf(n, p, "learn","study","course","school","college","university","quiz",
                "duolingo","ruangguru","zenius","coursera","udemy","khan","library",
                "read","book","dictionary","translate","language","english","math")) return "education";

        // Travel
        if (anyOf(n, p, "map","maps","navigation","gps","waze","traffic","transit",
                "travel","flight","hotel","traveloka","tiket","trip","tour","compass")) return "travel";

        // Tools
        if (anyOf(n, p, "setting","file","manager","cleaner","antivirus","vpn","browser",
                "chrome","firefox","opera","edge","calculator","clock","calendar","notes",
                "reminder","scanner","pdf","zip","backup","cloud","drive","dropbox",
                "terminal","ssh","adb","code","git","office","word","excel")) return "tools";

        // Default
        return "other";
    }

    private static boolean anyOf(String name, String pkg, String... keywords) {
        for (String k : keywords) {
            if (name.contains(k) || pkg.contains(k)) return true;
        }
        return false;
    }
}
