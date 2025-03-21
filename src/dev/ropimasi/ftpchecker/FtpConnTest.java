package dev.ropimasi.ftpchecker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;




public class FtpConnTest {

	public static void main(String[] args) {
		List<FtpServerRec> servers = new ArrayList<>();
		loadServerListFromDB(servers);

		for (FtpServerRec aServer : servers) {
			System.out.println("\nDEBUG: iteração.");
			boolean connected = testFtpConn(aServer.server(), aServer.port(), aServer.user(), aServer.pass());
			System.out.println(
					"Teste de conexão FTP em " + aServer.server() + ": " + (connected ? "Bem-sucedido" : "Falhou"));
		}
	}



	private static void loadServerListFromDB(List<FtpServerRec> servers) {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM server;")) {
			while (rs.next()) {
				servers.add(new FtpServerRec(rs.getString("host"), rs.getInt("port"), rs.getString("user"), rs.getString("pass")));
			}
		} catch (SQLException e) {
			System.err.println("Erro ao acessar o banco de dados: " + e.getMessage());
			return;
		}
	}



	public static Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/ftpchecker";
		String user = "postgres";
		String password = "postgres";
		return DriverManager.getConnection(url, user, password);
	}



	record FtpServerRec(String server, int port, String user, String pass) {
	}



	public static boolean testFtpConn(String server, int port, String user, String pass) {
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(server, port);
			int connResponse = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(connResponse)) {
				System.out.println("Conexão FTP recusada em " + server + ": " + connResponse);
				return false;
			}

			boolean login = ftpClient.login(user, pass);
			if (!login) {
				int loginResponseCode = ftpClient.getReplyCode();
				String loginResponseDescription = ftpClient.getReplyString();
				System.out.println("Falha no login FTP em " + server + ": " + loginResponseCode + " - "
						+ loginResponseDescription);
				return false;
			}

			System.out.println("Conexão FTP estabelecida com sucesso em " + server);

			ftpClient.logout();
			ftpClient.disconnect();

			return true;

		} catch (IOException ex) {
			System.out.println("Erro durante a conexão FTP em " + server + ": " + ex.getMessage());
			return false;
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ex) {
					// Ignora erros ao desconectar
				}
			}
		}
	}

}
