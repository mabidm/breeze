import scalanlp.stats._;
import scalanlp.stats.Rand._;
import scalanlp.stats.MarkovChain.Kernels._;

def sqr(x : Double) = x *x;

case class Params(mu : Double, sigma : Double);

val points = Rand.gaussian(10,5).sample(100);
val sampleMean = points.reduceLeft(_+_)/points.length;
val sampleVariance  = points.foldLeft(0.0)( (x,y) => x + sqr(y-sampleMean))/points.length

println("Sample Mean: " + sampleMean);
println("Sample Std: " + Math.sqrt(sampleVariance));

def ll(mu: Double, sigma:Double) = points.map(x => -sqr((x-mu)/sigma)/2).reduceLeft(_+_);
def postMu(mu : Double, sigma:Double) = ll(mu,sigma) + -sqr((mu-8)/1)/2;
def postSigma(mu : Double, sigma:Double) = ll(mu,sigma) + new Gamma(1,1).logProbabilityOf(sigma) - points.length *  Math.log(sigma);

val mc = MarkovChain(Params(0.0,2)) { case Params(mu,sigma) =>
  val muSampler = metropolis[Double](gaussian(_,3.0))(postMu(_,sigma));
  val sigmaSampler = slice(postSigma(mu,_),_>0);
  for(muNew <- muSampler(mu);
      sigmaNew <- sigmaSampler(sigma))
    yield Params(muNew,sigmaNew)
}

val chain = mc.sample(1000).drop(200);
println("E[mu]: " + chain.map(_.mu).reduceLeft(_+_)/chain.length);
println("E[sigma]: " + chain.map(_.sigma).reduceLeft(_+_)/chain.length);
