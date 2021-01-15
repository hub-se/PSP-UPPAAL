# Property Specification Patterns for UPPAAL (PSP-UPPAAL)

Specifying properties in a temporal logic is typically difficult and error-prone, even for experts in formal methods. To ease the specification of properties, specification patterns for qualitative [1], real-time [2] and probabilistic [3] properties have been proposed and eventually [aligned](http://ps-patterns.wikidot.com/) [4] in literature. However, they are not connected to existing model checkers and therefore not used in practice---despite their benefits of easing the specification.

In this project, we leverage qualitative and real-time specification patterns for the model checker [UPPAAL](https://uppaal.org/). As a consequence, properties to be verified do not have to be specified in Timed Computation Tree Logic (TCTL) directly. Instead, property specification patterns are used as a more user-friendly way to specify properties that are automatically translated to TCTL. Moreover, we realize patterns that cannot be expressed in the subset of TCTL supported by UPPAAL. In such cases, we generate observer automata based on templates that we defined for such patterns.

The result of this project is a **catalog of specification patterns for UPPAAL**. This catalog is documented in the wiki: [https://github.com/hub-se/PSP-UPPAAL/wiki](https://github.com/hub-se/PSP-UPPAAL/wiki) and the related templates for observer automata are available in this Git project.

## Team
* [Thomas Vogel](https://github.com/thomas-vogel)
* [Marc Carwehl](https://github.com/carwehlm)
* [Lars Grunske](https://github.com/larsgrunske)

## Acknowledgement
This work has been developed as part of the project [_Safe.Spec: Quality Assurance of Behavioral Requirements_](https://www.informatik.hu-berlin.de/en/forschung-en/gebiete/se/research/ongoingprojects/safespec/safespec) funded by the German Federal Ministry of Education and Research under Grant No. 01IS16027. 

## References

1. M. B. Dwyer, G. S. Avrunin, J. C. Corbett: Patterns in Property Specifications for Finite-State Verification. ICSE 1999. pp. 411-420.
2. S. Konrad and Betty H.C. Cheng: Real-time specification patterns. ICSE 2005. pp. 372-381.
3. L. Grunske: Specification patterns for probabilistic quality properties. ICSE 2008. pp. 31-40.
4. M. Autili, L. Grunske, M. Lumpe, P. Pelliccione, A. Tang: Aligning Qualitative, Real-Time, and Probabilistic Property Specification Patterns Using a Structured English Grammar. IEEE Trans. on Software Engineering, vol. 41, no. 7, 2015. pp. 620-638.
