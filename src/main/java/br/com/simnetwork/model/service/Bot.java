package br.com.simnetwork.model.service;

import java.util.List;

import br.com.simnetwork.model.entity.basico.usuario.Usuario;


public interface Bot {
	
	public void sendMessage(Usuario usuario, String mensagem);
	
	public void sendMessage(String usuario, String mensagem);
	
	public void sendMessage(List<Usuario> usuarios, String message);
	
	public void sendMessageWithoutKeyboard(String usuario, String mensagem);
	
	public void prepareKeyboard(List<String> strings, int n);
	
	public void prepareKeyboard(List<String> strings);

}
