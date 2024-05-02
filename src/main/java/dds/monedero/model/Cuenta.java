package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    /* Code smell: Duplicated code. Es redundante decir que el saldo va a estar en 0 si al momento de definir el atributo dijimos eso
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

  public void poner(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    // No usar el getter para acceder a un atributo de la misma clase
    if (this.movimientos.stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    new Movimiento(LocalDate.now(), cuanto, true).agregateA(this);
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    if (this.saldo - cuanto < 0) { // No usar el getter para acceder a un atributo de la misma clase
      throw new SaldoMenorException("No puede sacar mas de " + this.saldo + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    new Movimiento(LocalDate.now(), cuanto, false).agregateA(this);
  }

  public void agregarMovimiento(Movimiento movimiento) {
    /* Code Smell: Long Parameter List. En vez de pasarle los parámetros para armar el movimiento
     en este método, podemos recibir directamente el objeto instanciado. Esto nos da flexibilidad
      ya que si el constructor de movimiento cambia, no lo tendríamos que modificar acá
    * Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    * */
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return this.movimientos.stream() // No usar el getter para acceder a un atributo de la misma clase
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
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
  public void confirmarMovimiento(double monto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(LocalDate.now(), monto, esDeposito);
    this.agregarMovimiento(movimiento);
    this.saldo += monto * this.signoEsDeposito(esDeposito);
  }
}
