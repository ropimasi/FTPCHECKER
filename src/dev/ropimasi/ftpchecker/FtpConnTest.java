package dev.ropimasi.ftpchecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;




public class FtpConnTest {

	public static void main(String[] args) {
		List<FtpServerRec> servers = new ArrayList<>();
		servers.add(new FtpServerRec("servidor1", 21, "usuario1", "senha1"));
		servers.add(new FtpServerRec("servidor2", 21, "usuario2", "senha2"));
		// Adicione mais servidores conforme necessário

		for (FtpServerRec aServer : servers) {
			System.out.println("\nDEBUG:  iteração.");
			boolean connected = testFtpConn(aServer.server(), aServer.port(), aServer.user(),
					aServer.pass());
			System.out.println("Teste de conexão FTP em " + aServer.server() + ": "
					+ (connected ? "Bem-sucedido" : "Falhou"));
		}
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
				System.out.println(
						"Falha no login FTP em " + server + ": " + loginResponseCode + " - " + loginResponseDescription);
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
