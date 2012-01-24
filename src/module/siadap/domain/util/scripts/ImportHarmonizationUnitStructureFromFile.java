package module.siadap.domain.util.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.PartyType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.scheduler.ReadCustomTask;
import myorg.domain.scheduler.TransactionalThread;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

/**
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 16 de Dez de 2011
 * 
 *         Script that imports the harmonization units in the format: istId of
 *         the responsible of harmonization;Name of the harm. unit;cost center
 *         of the subunits
 */
public class ImportHarmonizationUnitStructureFromFile extends ReadCustomTask {

    public final static String csvContent = new String(
	    "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;3;CONSELHO DE GESTÃO\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;4;CONSELHO CIENTÍFICO\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;5;CONSELHO PEDAGÓGICO\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;6100;DIRECÇÃO EXECUTIVA - GERAL\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8001;NÚCLEO DE SECRETARIADO DO CG\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8002;NÚCLEO DE SERVIÇOS MÉDICOS E DE APOIO E AVAL. PSICOL.\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8003;IST PRESS\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8010;ASSESSORIAS AO CG\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8020;ÁREA DE ESTUDOS E PLANEAMENTO\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8021;NÚCLEO DE ESTATÍSTICA E PROSPECTIVA\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8030;ÁREA PARA A QUALIDADE E AUDITORIA INTERNA\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8040;ÁREA DE BIBLIOTECAS\n"
		    + "1;GESTÃO ADMINISTRATIVA E FINANCEIRA;ist12282;ARLINDO MANUEL LIMEDE DE OLIVEIRA;PRESIDENTE DO IST;8100;DIRECÇÃO DE APOIO JURÍDICO\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7610;GESTÃO DO CAMPUS DO IST/TAGUSPARK - GERAL\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7621;NÚCLEO FINANCEIRO DO TAGUSPARK\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7630;ÁREA TÉCNICA DO TAGUSPARK \n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7640;ÁREA ACADÉMICA E DE PESSOAL DO TAGUSPARK\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7650;ÁREA SERVIÇOS DE INFORMÁTICA DO TAGUSPARK\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7660;BIBLIOTECA DO TAGUSPARK\n"
		    + "2;GESTÃO DO CAMPUS DO IST NO TAGUSPARK;ist12922;TERESA MARIA SÁ FERREIRA VAZÃO VASQUES;VICE-PRESIDENTE PARA A GESTÃO DO CAMPUS DO TP;7670;NÚCLEO DE APOIO AO ESTUDANTE DO TAGUSPARK \n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6211;ÁREA CONTABILÍSTICA\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6212;NÚCLEO DE CONTABILIDADE\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6213;NÚCLEO DE EXECUÇÃO ORÇAMENTAL\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6215;NÚCLEO DE TESOURARIA\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6221;ÁREA ORÇAMENTAL E PATRIMONIAL\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6223;NÚCLEO DO PATRIMÓNIO\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6224;NÚCLEO DE COMPRAS E APROVISIONAMENTO\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6241;ÁREA DE PROJECTOS\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6242;NÚCLEO DE PROJECTOS NACIONAIS\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6243;NÚCLEO DE PROJECTOS DE CONSULTORIA E SERVIÇOS\n"
		    + "3;DIRECÇÃO FINANCEIRA;ist24235;NUNO ALEXANDRE DE BRITO PEDROSO;ADMINISTRADOR;6244;NÚCLEO DE PROJECTOS COMUNITÁRIOS\n"
		    + "4;ASSUNTOS INTERNACIONAIS;ist12760;JOSÉ ALBERTO ROSADO DOS SANTOS VICTOR;VICE PRESIDENTE PARA OS ASSUNTOS INTERNACIONAIS;8210;ÁREA DE ASSUNTOS INTERNACIONAIS\n"
		    + "4;ASSUNTOS INTERNACIONAIS;ist12760;JOSÉ ALBERTO ROSADO DOS SANTOS VICTOR;VICE PRESIDENTE PARA OS ASSUNTOS INTERNACIONAIS;8211;NÚCLEO DE MOBILID. E COOP. INTERNACIONAL\n"
		    + "4;ASSUNTOS INTERNACIONAIS;ist12760;JOSÉ ALBERTO ROSADO DOS SANTOS VICTOR;VICE PRESIDENTE PARA OS ASSUNTOS INTERNACIONAIS;8212;NÚCLEO DE RELAÇÕES INTERNACIONAIS\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6401;DIRECÇÃO DE RECURSOS HUMANOS\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6401;ASSESSORIA TÉCNICA\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6410;ÁREA COMUM DE RH\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6411;NÚCLEO DE ARQUIVO E DOCUMENTAÇÃO\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6412;NÚCLEO DE REMUNERAÇÕES, PROTEC. E BENEF. SOCIAIS\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6413;NÚCLEO DE PRESTAÇÃO DO TRABALHO\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6420;ÁREA ESPECIALIZADA DE RH\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6421;NÚCLEO DE DOCENTES E INVESTIGADORES\n"
		    + "5;DIRECÇÃO DE RECURSOS HUMANOS;ist23932;LUÍS MIGUEL MARQUES COIMBRA;DIRECTOR;6422;NÚCLEO DE NÃO DOCENTES E BOLSEIROS\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6301;DIRECÇÃO TÉCNICA\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6302;GESTÃO DE ESPAÇOS PAVILHÃO MATEMÁTICA E FÍSICA\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6306;GESTÃO ESP. COMPLEXO INTERDISCIPLINAR\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6311;ÁREA DE APOIO GERAL\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6312;NÚCLEO DE GESTÃO E ACOMPANHAMENTO DE CONTRATOS \n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6313;NÚCLEO DE ARQUIVO\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6314;NÚCLEO DE REPROGRAFIA\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6315;NÚCLEO DE SERVIÇOS GERAIS\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6316;NÚCLEO DE ALOJAMENTOS\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6331;ÁREA DE INSTALAÇÕES E EQUIPAMENTOS\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6332;NÚCLEO DE OBRAS\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6333;NÚCLEO DE MANUTENÇÃO\n"
		    + "6;DIRECÇÃO TÉCNICA;ist23470;JOSÉ MANUEL RAMOS RISCADO;DIRECTOR;6334;NÚCLEO DE SEGURANÇA, HIGIENE E SAÚDE\n"
		    + "7;DIRECÇÃO ACADÉMICA;ist23703;NUNO MIGUEL RAMOS RISCADO;DIRECTOR;8310;DIRECÇÃO ACADÉMICA\n"
		    + "7;DIRECÇÃO ACADÉMICA;ist23703;NUNO MIGUEL RAMOS RISCADO;DIRECTOR;8311;NÚCLEO DE GRADUAÇÃO\n"
		    + "7;DIRECÇÃO ACADÉMICA;ist23703;NUNO MIGUEL RAMOS RISCADO;DIRECTOR;8312;NÚCLEO DE PÓS-GRAD. E FORM. CONTÍNUA\n"
		    + "7;DIRECÇÃO ACADÉMICA;ist23703;NUNO MIGUEL RAMOS RISCADO;DIRECTOR;8313;GABINETE DE ORGANIZAÇÃO PEDAGÓGICA\n"
		    + "7;DIRECÇÃO ACADÉMICA;ist23703;NUNO MIGUEL RAMOS RISCADO;DIRECTOR;8314;GABINETE DE APOIO AO TUTORADO\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8401;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8410;ÁREA DE LIGAÇÃO AO UTILIZADOR\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8411;NÚCLEO DE MULTIMÉDIA E E-LEARNING\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8412;NÚCLEO DE SUPORTE AO UTILIZADOR\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8413;NÚCLEO DE MICROINFORMÁTICA\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8420;ÁREA DE INFRAESTRUTURAS\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8421;NÚCLEO DE REDES E SISTEMAS\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8422;NÚCLEO DE COMUNICAÇÕES DE VOZ E VÍDEO\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8430;ÁREA DE APLICAÇÕES E SISTEMAS DE INFORMAÇÃO\n"
		    + "8;DIRECÇÃO DE SERVIÇOS DE INFORMÁTICA;ist12048;FERNANDO HENRIQUE CORTE REAL MIRA DA SILVA;MEMBRO DO CG PARA AS TECNOLOGIAS DE INFORMAÇÃO E COMUNICAÇÃO;8431;NÚCLEO DE APLICAÇÕES ACADÉMICAS\n"
		    + "9;EMPREENDORISMO E LIGAÇÕES EMPRESARIAIS;ist12316;LUÍS MIGUEL VEIGA VAZ CALDAS DE OLIVEIRA;MEMBRO DO CG PARA O EMPREENDEDORISMO E LIGAÇÕES EMPRESARIAIS;8510;ÁREA DE TRANSFERÊNCIA DE TECNOLOGIA\n"
		    + "9;EMPREENDORISMO E LIGAÇÕES EMPRESARIAIS;ist12316;LUÍS MIGUEL VEIGA VAZ CALDAS DE OLIVEIRA;MEMBRO DO CG PARA O EMPREENDEDORISMO E LIGAÇÕES EMPRESARIAIS;8511;NÚCLEO DE PROPRIEDADE INTELECTUAL\n"
		    + "9;EMPREENDORISMO E LIGAÇÕES EMPRESARIAIS;ist12316;LUÍS MIGUEL VEIGA VAZ CALDAS DE OLIVEIRA;MEMBRO DO CG PARA O EMPREENDEDORISMO E LIGAÇÕES EMPRESARIAIS;8512;NÚCLEO DE PARCERIAS EMPRESARIAIS\n"
		    + "10;COMUNICAÇÃO E IMAGEM;ist12451;PALMIRA MARIA MARTINS FERREIRA DA SILVA;MEMBRO DO CG PARA A COMUNICAÇÃO E IMAGEM;8610;ÁREA DE COMUNICAÇÃO E IMAGEM\n"
		    + "10;COMUNICAÇÃO E IMAGEM;ist12451;PALMIRA MARIA MARTINS FERREIRA DA SILVA;MEMBRO DO CG PARA A COMUNICAÇÃO E IMAGEM;8611;GABINETE DE COMUNICAÇÕES E RELAÇÕES PÚBLICAS\n"
		    + "10;COMUNICAÇÃO E IMAGEM;ist12451;PALMIRA MARIA MARTINS FERREIRA DA SILVA;MEMBRO DO CG PARA A COMUNICAÇÃO E IMAGEM;8612;NÚCLEO DE GESTÃO DO MUSEU E CENTRO DE CONGRESSOS\n"
		    + "10;COMUNICAÇÃO E IMAGEM;ist12451;PALMIRA MARIA MARTINS FERREIRA DA SILVA;MEMBRO DO CG PARA A COMUNICAÇÃO E IMAGEM;8613;NÚCLEO DE APOIO AO ESTUDANTE\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist11432;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;1144;CENTRO ENG.ª BIOLÓGICA E QUÍMICA / INSTITUTO DE BIOTECNOLOGIA E BIOENGENHARIA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist11432;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;1604;ICEMS\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist11432;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;2301;DEQB - PRESIDÊNCIA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist11432;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;2320;BIBLIOTECA DO DEP. DE ENG.ª QUÍMICA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist11432;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;3301;DEPARTAMENTO DE BIOENGENHARIA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist12081;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;1144;CENTRO ENG.ª BIOLÓGICA E QUÍMICA / INSTITUTO DE BIOTECNOLOGIA E BIOENGENHARIA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist12081;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;1604;ICEMS\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist12081;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;2301;DEQB - PRESIDÊNCIA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist12081;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;2320;BIBLIOTECA DO DEP. DE ENG.ª QUÍMICA\n"
		    + "11;DEPARTAMENTO DE BIOENGENHARIA / DEPARTAMENTO DE ENG. QUÍMICA;ist12081;JOAQUIM MANUEL SAMPAIO CABRAL / FRANCISCO MANUEL DA SILVA LEMOS;PRESIDENTE DO DEPARTAMENTO;3301;DEPARTAMENTO DE BIOENGENHARIA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;1102;CENTRO ESTUDOS E HIDROSISTEMA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;1126;CENTRO SIST. URBANOS/REGIONAIS\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;1143;CENTRO DE RECURSOS NATURAIS E AMBIENTE\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;1608;ICIST - INSTITUTO CONSTRUÇÃO\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2001;DECA-PRESIDÊNCIA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2003;SEC. DE HIDRÁULICA E DOS RECURSOS HÍDRICOS E AMBIENTAIS \n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2009;SEC. URBANISMO, TRANSPORTES, VIAS E SISTEMA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2010;SEC. GEOTECNIA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2011;SEC. SISTEMAS DE APOIO AO PROJECTO\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2012;SEC. ARQUITECTURA\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2013;SEC. MECÂNICA ESTRUTURAL E ESTRUTURAS\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2014;SECÇÃO DE CONSTRUÇÃO\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2015;SECÇÃO DE MINAS E GEORRECURSOS\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2020;BIBLIOTECA DO DECivil, ARQUITECTURA E GEORRECURSOS\n"
		    + "12;DEPARTAMENTO DE ENG.ª CIVIL, ARQUITECTURA E GEORRECURSOS;ist11387;JOÃO JOSÉ RIO TINTO DE AZEVEDO;PRESIDENTE DO DECivil;2040;CONTABILIDADE INTEGRADA DO DECivil, ARQUITECTURA E GEORRECURSOS\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;1146;CENTRO PARA A INOVAÇÃO EM ENGENHARIA ELECTROTÉCNICA E ENERGIA\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;1601;POLO IST-ISR\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;1603;POLO IST - INSTITUTO DE TELECOMUNICACOES\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2101;DEEC-PRESIDÊNCIA\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2110;DEEC - TAGUSPARK\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2111;ÁREA CIENTÍFICA DE COMPUTADORES\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2112;ÁREA CIENTÍFICA DE ELECTRÓNICA\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2113;ÁREA CIENTÍFICA DE ENERGIA\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2114;ÁREA CIENTÍFICA DE SISTEMAS DECIS E CONTROLO\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2115;ÁREA CIENTÍFICA DE TELECOMUNICAÇÕES\n"
		    + "13;DEPARTAMENTO DE ENG.º ELECTROTÉCNICA E COMPUTADORES;ist11546;PEDRO MANUEL BRITO DA SILVA GIRÃO;PRESIDENTE DO DEEC;2120;BIBLIOTECA DEEC\n"
		    + "14;DEPARTAMENTO DE ENG.ª E GESTÃO;ist12037;CARLOS ANTÓNIO BANA E COSTA;PRESIDENTE DO DEG;1134;CENTRO ESTUDOS DE GESTÃO - IST\n"
		    + "14;DEPARTAMENTO DE ENG.ª E GESTÃO;ist12037;CARLOS ANTÓNIO BANA E COSTA;PRESIDENTE DO DEG;2901;DEG - PRESIDÊNCIA\n"
		    + "15;DEPARTAMENTO DE ENG.ª INFORMÁTICA;ist126480;LUÍS EDUARDO TEIXEIRA RODRIGUES ;PRESIDENTE DO DEI;2801;DEI  - PRESIDÊNCIA\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;1133;UNIDADE DE ENG.ª TECNOLOGIA NAVAL\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;1137;IN+, C.ESTUDOS INOV. TECN. POLITICAS DES.\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;1602;POLO IST-IDMEC\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2201;DEP. ENG.ª MECÂNICA - PRESIDÊNCA\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2210;ÁREA CIENTÍFICA DE AMBIENTE E ENERGIA\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2211;ÁREA CIENTÍFICA DE CONTROLO, AUT. INF. INDUSTRIAL\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2212;ÁREA CIENTÍFICA DE MECÂNICA APLIC. E AEROESPACIAL\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2213;ÁREA CIENTÍFICA DE MECÂNICA ESTRUTURAL E COMPUT.\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2214;ÁREA CIENTÍFICA DE PROJECTO MECÂNICO E MAT. ESTR.\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2215;ÁREA CIENTÍFICA DE TECNOLOGIA MECÂNICA E G. INDUS.\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2216;ÁREA CIENTÍFICA DE TERMOFLUIDOS E TECNOL.CONV.EN.\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2217;ÁREA CIENTÍFICA DE ENG.ª NAVAL\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;2220;BIBLIOTECA DO DEP. DE ENG.ª MECÂNICA\n"
		    + "16;DEPARTAMENTO DE ENG.ª MECÂNICA;ist12094;HÉLDER CARRIÇO RODRIGUES;PRESIDENTE DO DEM;4015;COORD. DA LICENCIATURA DE ENG.ª DO AMBIENTE\n"
		    + "18;DEPARTAMENTO DE FÍSICA;ist11945;ALFREDO BARBOSA HENRIQUES;PRESIDENTE DO DF;2401;DEP. FÍSICA - PRESIDÊNCIA\n"
		    + "18;DEPARTAMENTO DE FÍSICA;ist11945;ALFREDO BARBOSA HENRIQUES;PRESIDENTE DO DF;2420;BIBLIOTECA DO DEP. DE FÍSICA\n"
		    + "19;DEPARTAMENTO DE MATEMÁTICA;ist12530;RUI ANTÓNIO LOJA FERNANDES;PRESIDENTE DO DM;2501;DEP. MATEMÁTICA - PRESIDÊNCIA\n"
		    + "20;INSTITUTO DE PLASMAS E FUSÃO NUCLEAR;ist11063;CARLOS ANTÓNIO ABREU FONSECA VARANDAS;PRESIDENTE DO IPFN;1145;INSTITUTO DE PLASMAS E FUSÃO NUCLEAR\n"
		    + "21;COMPLEXO INTERDISCIPLINAR;ist10984;JOSÉ MANUEL GASPAR MARTINHO;PRESIDENTE DO COMPLEXO I;1113;CENTRO DE QUÍMICA ESTRUTURAL\n"
		    + "21;COMPLEXO INTERDISCIPLINAR;ist10984;JOSÉ MANUEL GASPAR MARTINHO;PRESIDENTE DO COMPLEXO I;1114;CENTRO DE FÍSICA MOLECULAR\n"
		    + "21;COMPLEXO INTERDISCIPLINAR;ist10984;JOSÉ MANUEL GASPAR MARTINHO;PRESIDENTE DO COMPLEXO I;1115;CENTRO DE ANÁLISES E PROC. SINAIS\n"
		    + "21;COMPLEXO INTERDISCIPLINAR;ist10984;JOSÉ MANUEL GASPAR MARTINHO;PRESIDENTE DO COMPLEXO I;1116;CENTRO DE QUÍMICA-FÍSICA MOLECULAR\n"
		    + "21;COMPLEXO INTERDISCIPLINAR;ist10984;JOSÉ MANUEL GASPAR MARTINHO;PRESIDENTE DO COMPLEXO I;6202;COORD. DE SERVIÇOS ADMINISTRATIVOS E FINANCEIROS DO COMPLEXO \n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8050;LABORATÓRIO DE ANÁLISES\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8051;NÚCLEO ANÁL. GER. APLIC. ÁGUAS LIMPAS\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8052;NÚCLEO ANÁL. GER. APLIC. ÁGUAS RESIDUAIS\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8053;NÚCLEO ANÁL. COMPOSTOS ORGÂNICOS\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8054;NÚCLEO METAIS PREP. AMOSTRAS SÓLIDAS\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8055;NÚCLEO DE MICROBIOLOGIA\n"
		    + "22;LABORATÓRIO DE ANÁLISES;ist11648;MARGARIDA MARIA PORTELA CORREIA DOS SANTOS ROMÃO;DIRECTORA ADJUNTA DO LAIST;8056;NÚC. GEST. COLHEIT. AMB. SAÚDE E SEGURANÇA\n");

    public final static HashMap<String, HarmonizationUnit> inferedUnits = new HashMap<String, HarmonizationUnit>();
    public final int yearToUse = 2011;
    private static final LocalDate DATE_TO_USE = new LocalDate(2011, 12, 20);

    class HarmonizationUnit {
	private final String unitName;
	private final Set<Unit> subUnits;
	private final Set<Person> harmonizationResponsibles = new HashSet<Person>();
	private final String unitAcronym;
	private final static String unitNamePrefix = "SIADAP - U.H. ";

	public HarmonizationUnit(String unitAcronym, String unitName, User harmonizationResponsible) {
	    subUnits = new HashSet<Unit>();
	    this.unitName = unitName;
	    this.harmonizationResponsibles.add(harmonizationResponsible.getPerson());
	    this.unitAcronym = unitNamePrefix + unitAcronym;
	}

	public void addCC(Unit ccUnit) {
	    getSubUnits().add(ccUnit);
	}

	public Set<Unit> getSubUnits() {
	    return subUnits;
	}

	public Set<Person> getHarmonizationResponsibles() {
	    return harmonizationResponsibles;
	}

	public String getUnitName() {
	    return unitName;
	}

	public void addHarmonizationResponsible(User harmonizationResponsible) {
	    this.harmonizationResponsibles.add(harmonizationResponsible.getPerson());
	}

    }

    @Override
    public void doIt() {

	//	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(yearToUse);

	//let's read each line and create the appropriate objects
	try {
	    StringReader csvContentReader = new StringReader(csvContent);
	    BufferedReader br = new BufferedReader(csvContentReader);
	    String strLine;
	    // Read File Line By Line
	    while ((strLine = br.readLine()) != null) {
		processLine(strLine);

	    }
	} catch (IOException e) {// Catch exception if any
	    out.println("Error: " + e.getMessage());
	}

	//print what you got
	for (HarmonizationUnit harmUnit : inferedUnits.values()) {
	    for (Unit costCenter : harmUnit.getSubUnits()) {
		for (Person responsible : harmUnit.getHarmonizationResponsibles()) {
		    out.println(harmUnit.unitAcronym + ";" + responsible.getUser().getUsername() + ";"
			+ harmUnit.getUnitName() + ";"
			+ costCenter.getAcronym());

		}

	    }
	}

	ProcessHarmonizationUnits processHarmonizationUnits = new ProcessHarmonizationUnits(inferedUnits.values());
	processHarmonizationUnits.start();
	try {
	    processHarmonizationUnits.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    e.printStackTrace(out);
	    throw new Error(e);
	}

    }

    class ProcessHarmonizationUnits extends TransactionalThread {

	final Collection<HarmonizationUnit> harmonizationUnits;
	private final AccountabilityType unitRelations;
	private final AccountabilityType unitHarmonizationRelation;
	private final Unit siadapStructureTopUnit;
	private final PartyType siadapHarmonizationUnitType;
	private OrganizationalModel organizationModelToUse;
	private final int unitsCount;
	private final AccountabilityType harmonizationResponsibleRelation;

	ProcessHarmonizationUnits(Collection<HarmonizationUnit> harmonizationUnits) {
	    this.harmonizationUnits = harmonizationUnits;
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(yearToUse);
	    unitRelations = siadapYearConfiguration.getUnitRelations();
	    unitHarmonizationRelation = siadapYearConfiguration.getHarmonizationUnitRelations();
	    siadapStructureTopUnit = siadapYearConfiguration.getSiadapStructureTopUnit();
	    //	    for (HarmonizationUnit harmonizationUnit : harmonizationUnits)
	    //	    {
	    //		for (Unit subUnit : harmonizationUnit.getSubUnits())
	    //		{
	    //		    subUnit.getOr
	    //		}
	    //	    }

	    siadapHarmonizationUnitType = PartyType.readBy(UnitSiadapWrapper.SIADAP_HARMONIZATION_UNIT_TYPE);
	    for (OrganizationalModel organizationModel : siadapStructureTopUnit.getOrganizationalModels()) {
		if (organizationModel.getName().equalInAnyLanguage(UnitSiadapWrapper.SIADAP_ORGANIZATION_MODEL_NAME))
		    organizationModelToUse = organizationModel;
	    }
	    harmonizationResponsibleRelation = siadapYearConfiguration.getHarmonizationResponsibleRelation();
	    this.unitsCount = siadapStructureTopUnit.getChildren(unitRelations).size();

	}

	@Override
	public void transactionalRun() {
	    ArrayList<AccountabilityType> accTypesToSearchFor = new ArrayList<AccountabilityType>();
	    accTypesToSearchFor.add(unitHarmonizationRelation);
	    accTypesToSearchFor.add(unitRelations);
	    for (HarmonizationUnit harmonizationUnit : harmonizationUnits) {
		debugLn("Processing unit " + harmonizationUnit.getUnitName());
		//let's find out if we have a unit with this name already
		Unit unitToUse = null;
		for (Unit unit : siadapStructureTopUnit.getChildUnits(accTypesToSearchFor)) {
		    if (unit.getPartyName().equalInAnyLanguage(harmonizationUnit.getUnitName())
			    && unit.getPartyTypes().contains(siadapHarmonizationUnitType)) {
			unitToUse = unit;
			break;
		    }
		}

		//let's create it if we need to
		if (unitToUse == null) {
		    debugLn(" -- creating it");
		    unitToUse = Unit.create(siadapStructureTopUnit, new MultiLanguageString(harmonizationUnit.getUnitName()),
			    harmonizationUnit.unitAcronym, siadapHarmonizationUnitType, unitHarmonizationRelation,
			    DATE_TO_USE, lastDayOfYear(yearToUse), organizationModelToUse);
		} else
		    debugLn(" -- It already existed");
		//so now let's just add all of the subunits
		Collection<Unit> previousChildUnits = unitToUse.getChildUnits(accTypesToSearchFor);
		for (Unit subUnit : harmonizationUnit.getSubUnits()) {
		    if (!previousChildUnits.contains(subUnit)) {
			debugLn(" ---- creating the relation with " + subUnit.getPartyName().getContent());
			subUnit.addParent(unitToUse, unitHarmonizationRelation, DATE_TO_USE, lastDayOfYear(yearToUse));
		    } else {
			debugLn(" ---- relation with " + subUnit.getPartyName().getContent() + " already existed");
			//let's check on the relation itself and make it right
			for (Accountability acc : subUnit.getParentAccountabilities(accTypesToSearchFor)) {
			    if (acc.getParent().equals(unitToUse)) {
				if (!acc.getEndDate().equals(lastDayOfYear(yearToUse))) {
				    //if the end date is wrong, fix it
				    acc.editDates(acc.getBeginDate(), lastDayOfYear(yearToUse));
				    debugLn(" ----- relation existed but with incorrect end date. Fixed");
				}
				if (!acc.getAccountabilityType().equals(unitHarmonizationRelation)) {
				    //if the acc. type is not right, let's set it straight
				    acc.setAccountabilityType(unitHarmonizationRelation);
				    debugLn(" ----- relation existed but with incorrect Acc type. Fixed");
				}
			    }
			}
		    }
		}
		//let's remove the ones that don't belong
		for (Unit previousChildUnit : previousChildUnits) {
		    if (!harmonizationUnit.getSubUnits().contains(previousChildUnit))
		    //let's remove it
		    {
			for (Accountability previousAccs : previousChildUnit.getParentAccountabilities(accTypesToSearchFor)) {
			    if (previousAccs.getParent().equals(unitToUse)) {
				//this is one that should be removed
				debugLn(" ---- removing relation with " + previousChildUnit.getPartyName().getContent());
				previousChildUnit.removeParent(previousAccs);
			    }
			}
		    }
		}
		
		//and now let's take care of the responsible(s)
		Collection<Person> previousResponsibles = unitToUse.getChildPersons(harmonizationResponsibleRelation);
		Set<Person> responsiblesToAddNewAccsFor = new HashSet<Person>(harmonizationUnit.getHarmonizationResponsibles());
		for (Accountability acc : unitToUse.getChildAccountabilities())
		{
		    if (acc.getAccountabilityType().equals(harmonizationResponsibleRelation))
		    {

			for (Person responsible : harmonizationUnit.getHarmonizationResponsibles()) {

			    if (harmonizationUnit.getHarmonizationResponsibles().contains(acc.getChild()))
			{
			    //			    if (acc.isActiveNow()) {
				//make sure that it will be active until the end of the year
			    debugLn(" ------ Acc. for responsible added/augmented");
				acc = acc.editDates(acc.getBeginDate(), lastDayOfYear(yearToUse));
			    //			    }
				if (acc.getChild().equals(responsible)) {
				    responsiblesToAddNewAccsFor.remove(responsible);
				}
			}
			else if (acc.isActiveNow())
			{
			    //let's 'remove' it
			    debugLn(" ------ Acc. for previous responsible ended on the date to use: " + DATE_TO_USE.toString());
			    acc.editDates(acc.getBeginDate(), DATE_TO_USE);
			}
			}
		    }
		}
		if (!responsiblesToAddNewAccsFor.isEmpty())
		{
		    for (Person harmResponsible : responsiblesToAddNewAccsFor) {
			//let's add the responsible
			unitToUse.addChild(harmResponsible, harmonizationResponsibleRelation, DATE_TO_USE,
				lastDayOfYear(yearToUse));
			debugLn(" ------ Added the responsible, no previous relation was found");

		    }
		}

	    }

	}
    }

    private void debugLn(String message) {
	if (true)
	    out.println(message);
    }

    private LocalDate lastDayOfYear(int year) {
	return new LocalDate(year, 12, 31);
    }

    private void processLine(String strLine) {

	String[] values = strLine.split(";");

	if (values.length != 7) {
	    out.println("skipped: " + strLine);
	    return;
	}

	String harmUnitNameAcronym = values[0].trim();
	String istIdHarmResponsible = values[2].trim();
	String harmUnitName = values[1].trim();
	Integer ccNumber = Integer.valueOf(values[5].trim());

	if (StringUtils.isBlank(istIdHarmResponsible) || StringUtils.isBlank(harmUnitName)
		|| User.findByUsername(istIdHarmResponsible) == null || StringUtils.isBlank(harmUnitNameAcronym)) {
	    out.println("skipped: " + strLine + "could not get needed values");
	    return;
	}

	User istIdHarmUser = User.findByUsername(istIdHarmResponsible);

	if (istIdHarmUser == null)
	    throw new DomainException("could.not.find.user:" + istIdHarmResponsible);

	debugLn("processing CC: " + ccNumber);
	//we must do a little hack to ensure that we have ccNumber.toString() with 4 digits
	String ccNumberString = ccNumber.toString();
	if (ccNumberString.length() < 4) {
	    //we have to add zeros
	    while (ccNumberString.length() < 4) {
		ccNumberString = "0" + ccNumberString;
	    }
	}
	CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(ccNumberString);
	Unit ccUnit = c.getUnit();
	//	debugLn("proccessed CC: " + ccNumber);

	//let's try to get the harmonization unit
	HarmonizationUnit harmonizationUnit = inferedUnits.get(harmUnitName);
	if (harmonizationUnit == null) {
	    harmonizationUnit = new HarmonizationUnit(harmUnitNameAcronym, harmUnitName, istIdHarmUser);
	}
	harmonizationUnit.addHarmonizationResponsible(istIdHarmUser);
	harmonizationUnit.addCC(ccUnit);

	inferedUnits.put(harmUnitName, harmonizationUnit);

    }

}