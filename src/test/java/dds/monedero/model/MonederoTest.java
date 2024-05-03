package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonederoTest {
  private Cuenta cuenta;
  private Cuenta cuentaCon500Pesos;

  @BeforeEach
  void init() {
    cuenta = new Cuenta();
    cuentaCon500Pesos = new Cuenta(500, new ArrayList<>());
  }

  @Test
  void Saldo500Pesos() {
    assertEquals(500, cuentaCon500Pesos.getSaldo());
  }

  @Test
  void SaldoSinMovimientos() {
    assertEquals(0, cuenta.getSaldo());
  }

  @Test
  void Poner() {
    cuenta.poner(1500);
    // Fixeo el test vacío
    assertEquals(1500, cuenta.getSaldo());
  }

  @Test
  void PonerMontoNegativo() {
    assertThrows(MontoNegativoException.class, () -> cuenta.poner(-1500));
  }

  @Test
  void TresDepositos() {
    cuenta.poner(1500);
    cuenta.poner(456);
    cuenta.poner(1900);
    // Fixeo el test vacío
    assertEquals(3856, cuenta.getSaldo());
  }

  @Test
  void RealizarMasDe3Depositos() {
    assertThrows(MaximaCantidadDepositosException.class, () -> {
          cuenta.poner(1500);
          cuenta.poner(456);
          cuenta.poner(1900);
          cuenta.poner(245);
    });
  }

  @Test
  void ExtraerMasQueElSaldo() {
    assertThrows(SaldoMenorException.class, () -> {
          cuenta.setSaldo(90);
          cuenta.sacar(1001);
    });
  }

  @Test
  public void ExtraerMasDe1000() {
    assertThrows(MaximoExtraccionDiarioException.class, () -> {
      cuenta.setSaldo(5000);
      cuenta.sacar(1001);
    });
  }

  @Test
  public void ExtraerMontoNegativo() {
    assertThrows(MontoNegativoException.class, () -> cuenta.sacar(-500));
  }

}