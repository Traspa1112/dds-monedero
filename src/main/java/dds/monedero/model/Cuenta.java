package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    /* Code smell: Duplicated code. Es redundante decir que el saldo va a estar en 0 si al
    momento de definir el atributo dijimos eso
    saldo = 0;*/
  }

  public Cuenta(double montoInicial, List<Movimiento> movimientos) {
    saldo = montoInicial;
    this.movimientos = movimientos;
  }

  /* Podría estar directamente en el constructor ya que para agregar movimientos lo hacemos con
  el método `agregarMovimiento`
  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }*/

  public Stream<Movimiento> movimientosDepositadosEn(LocalDate fechaDeposito) {
    return this.movimientos.stream().filter(movimiento -> movimiento.fueDepositado(fechaDeposito));
  }

  // Creo que este es un Code Smell: Duplicated code con el método `sacar`, pero no sabría cómo
  // resolverlo
  public void poner(double cuanto) {
    int cantidadDepositosDiarios = 3;
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    // Code Smell: Long Method. Abstraemos el código que filtra si los movimientos fueron
    // depositados al método movimientosDepositadosEn para permitir la reutilización del mismo.
    // Por otra parte resolvemos el code smell de Message Chains
    if (movimientosDepositadosEn(LocalDate.now()).count() >= cantidadDepositosDiarios) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + cantidadDepositosDiarios + " depositos diarios");
    }

    confirmarMovimiento(new Movimiento(LocalDate.now(), cuanto, true));
  }

  double limiteDiario = 1000;

  public double limiteDiarioRestante() {
    return limiteDiario - getMontoExtraidoA(LocalDate.now());
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (this.saldo - cuanto < 0) { // No usar el getter para acceder a un atributo de la misma clase
      throw new SaldoMenorException("No puede sacar mas de " + this.saldo + " $");
    }

    /* Code Smell: Temporary field. No es necesario guardar el limite siendo que calcularlo no es
     tan costoso
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    */

    if (cuanto > limiteDiarioRestante()) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + limiteDiario
          + " diarios, límite: " + limiteDiarioRestante());
    }

    confirmarMovimiento(new Movimiento(LocalDate.now(), cuanto, false));
  }

  public Stream<Movimiento> movimientosExtraidosEn(LocalDate fechaExtraccion) {
    return this.movimientos.stream().filter(movimiento -> movimiento.fueExtraido(fechaExtraccion));
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    // Code Smell: Long Method. Abstraemos el código que filtra si los movimientos fueron
    // extraídos al método movimientosExtraidosEn para permitir la reutilización del mismo.
    // Por otra parte resolvemos el code smell de Message Chains
    return movimientosExtraidosEn(LocalDate.now())
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public int signoEsDeposito(boolean esDeposito) {
    return esDeposito ? 1 : -1;
  }

  // Code smell: Feature Envy El método `confirmarMovimiento` reemplaza a `agregateA(Cuenta
  // cuenta)` y `calcularValor (Cuenta cuenta)` de la clase Movimiento
  public void confirmarMovimiento(Movimiento movimiento) {
    /* Code Smell: Long Parameter List. En vez de pasarle los parámetros para armar el movimiento
      en este método, podemos recibir directamente el objeto instanciado. Esto nos da flexibilidad
      ya que si el constructor de movimiento cambia, no lo tendríamos que modificar acá
      Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    */
    movimientos.add(movimiento);
    this.saldo += movimiento.getMonto() * this.signoEsDeposito(movimiento.isDeposito());
  }
}
