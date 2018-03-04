package renor.misc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ChatAllowedCharacters {
	public static final String allowedCharacters = getAllowedCharacters();

	private static String getAllowedCharacters() {
		String result = "";

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(ChatAllowedCharacters.class.getResourceAsStream("/font.txt"), "UTF-8"));
			String line = "";

			while ((line = reader.readLine()) != null)
				if (!line.startsWith("#")) result = result + line;

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
